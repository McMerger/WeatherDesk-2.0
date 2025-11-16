package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import service.AuthService
import service.FirebaseService
import model.Location

suspend fun ApplicationCall.authenticate(): String? {
    val token = request.headers["Authorization"]?.removePrefix("Bearer ")
    if (token.isNullOrBlank()) {
        respond(HttpStatusCode.Unauthorized, "Missing token")
        return null
    }

    return try {
        AuthService.verifyIdToken(token)
    } catch (e: Exception) {
        respond(HttpStatusCode.Unauthorized, "Invalid token")
        null
    }
}

fun Application.apiRoutes() {
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    routing {
        get("/weather") {
            val latitude = call.parameters["latitude"]?.toDoubleOrNull()
            val longitude = call.parameters["longitude"]?.toDoubleOrNull()

            if (latitude == null || longitude == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or invalid latitude/longitude parameters")
                return@get
            }

            try {
                val weatherResponse = httpClient.get("https://api.open-meteo.com/v1/forecast") {
                    parameter("latitude", latitude)
                    parameter("longitude", longitude)
                    parameter("current", "temperature_2m,relative_humidity_2m,is_day,wind_speed_10m,weather_code")
                    parameter("daily", "temperature_2m_max,temperature_2m_min,weather_code")
                    parameter("temperature_unit", "fahrenheit")
                    parameter("wind_speed_unit", "mph")
                    parameter("forecast_days", "7")
                }

                val responseBody = weatherResponse.bodyAsText()
                val openMeteoData = Json.parseToJsonElement(responseBody).jsonObject

                // Transform Open-Meteo response to match frontend expectations
                val current = openMeteoData["current"]?.jsonObject
                val daily = openMeteoData["daily"]?.jsonObject
                val transformedResponse = buildJsonObject {
                    put("longitude", openMeteoData["longitude"] ?: JsonPrimitive(longitude))
                    put("latitude", openMeteoData["latitude"] ?: JsonPrimitive(latitude))
                    putJsonObject("current") {
                        put("temperature", current?.get("temperature_2m") ?: JsonPrimitive(0))
                        put("relativeHumidity", current?.get("relative_humidity_2m") ?: JsonPrimitive(0))
                        put("isDaytime", current?.get("is_day") ?: JsonPrimitive(1))
                        put("windSpeed", current?.get("wind_speed_10m") ?: JsonPrimitive(0))
                        put("weatherCode", current?.get("weather_code") ?: JsonPrimitive(0))
                    }
                    putJsonObject("daily") {
                        put("time", daily?.get("time") ?: JsonPrimitive("[]"))
                        put("temperatureMax", daily?.get("temperature_2m_max") ?: JsonPrimitive("[]"))
                        put("temperatureMin", daily?.get("temperature_2m_min") ?: JsonPrimitive("[]"))
                        put("weatherCode", daily?.get("weather_code") ?: JsonPrimitive("[]"))
                    }
                }

                call.respond(HttpStatusCode.OK, transformedResponse)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to fetch weather data: ${e.message}")
            }
        }

        post("/location") {
            val userId = call.authenticate() ?: return@post
            val location = call.receive<Location>()
            FirebaseService.saveLastLocation(userId, location)
            call.respond(HttpStatusCode.OK, "Location saved.")
        }

        get("/location") {
            val userId = call.authenticate() ?: return@get
            val location = FirebaseService.getLastLocation(userId)
            if (location != null) {
                call.respond(HttpStatusCode.OK, location)
            } else {
                call.respond(HttpStatusCode.NotFound, "No location found.")
            }
        }

        post("/locations/saved") {
            val userId = call.authenticate() ?: return@post
            val location = try {
                call.receive<Location>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid location payload.")
                return@post
            }
            FirebaseService.addToSavedLocations(userId, location)
            call.respond(HttpStatusCode.OK, "Saved location added.")
        }

        get("/locations/saved") {
            val userId = call.authenticate() ?: return@get
            val saved = FirebaseService.getSavedLocations(userId)
            call.respond(HttpStatusCode.OK, saved)
        }

        delete("/locations/saved/{name}") {
            val userId = call.authenticate() ?: return@delete
            val name = call.parameters["name"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing location name.")
                return@delete
            }
            FirebaseService.removeSavedLocation(userId, name)
            call.respond(HttpStatusCode.OK, "Saved location removed.")
        }
    }
}