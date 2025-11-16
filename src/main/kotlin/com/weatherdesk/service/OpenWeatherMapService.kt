package com.weatherdesk.service

import com.google.gson.annotations.SerializedName
import com.weatherdesk.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*
import mu.KotlinLogging
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

/**
 * OpenWeatherMap API response models
 */
data class WeatherResponse(
    val coord: Coord?,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val dt: Long,
    val name: String
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    val humidity: Int,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double
)

data class Wind(
    val speed: Double
)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>
)

data class City(
    val name: String,
    val coord: Coord
)

/**
 * Service for interacting with OpenWeatherMap API
 */
class OpenWeatherMapService(private val apiKey: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    private val baseUrl = "https://api.openweathermap.org/data/2.5"

    /**
     * Fetch current weather by city name
     */
    suspend fun getCurrentWeatherByCity(city: String): WeatherData {
        logger.info { "Fetching weather for city: $city" }

        return try {
            val currentWeatherUrl = "$baseUrl/weather?q=$city&appid=$apiKey&units=metric"
            val currentResponse: WeatherResponse = client.get(currentWeatherUrl).body()

            val forecastUrl = "$baseUrl/forecast?q=$city&appid=$apiKey&units=metric"
            val forecastResponse: ForecastResponse = client.get(forecastUrl).body()

            mapToWeatherData(currentResponse, forecastResponse)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching weather for city: $city" }
            throw WeatherServiceException("Failed to fetch weather for $city: ${e.message}", e)
        }
    }

    /**
     * Fetch current weather by coordinates
     */
    suspend fun getCurrentWeatherByCoordinates(lat: Double, lon: Double): WeatherData {
        logger.info { "Fetching weather for coordinates: ($lat, $lon)" }

        return try {
            val currentWeatherUrl = "$baseUrl/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"
            val currentResponse: WeatherResponse = client.get(currentWeatherUrl).body()

            val forecastUrl = "$baseUrl/forecast?lat=$lat&lon=$lon&appid=$apiKey&units=metric"
            val forecastResponse: ForecastResponse = client.get(forecastUrl).body()

            mapToWeatherData(currentResponse, forecastResponse)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching weather for coordinates: ($lat, $lon)" }
            throw WeatherServiceException("Failed to fetch weather for coordinates: ${e.message}", e)
        }
    }

    /**
     * Fetch weather by location input
     */
    suspend fun getWeather(location: LocationInput): WeatherData {
        return when (location) {
            is LocationInput.City -> getCurrentWeatherByCity(location.name)
            is LocationInput.Coordinates -> getCurrentWeatherByCoordinates(
                location.latitude,
                location.longitude
            )
        }
    }

    /**
     * Map API responses to domain model
     */
    private fun mapToWeatherData(
        currentResponse: WeatherResponse,
        forecastResponse: ForecastResponse
    ): WeatherData {
        val current = CurrentWeather(
            city = currentResponse.name,
            temperatureCelsius = currentResponse.main.temp,
            condition = WeatherCondition.fromString(currentResponse.weather.firstOrNull()?.main ?: "Unknown"),
            conditionDescription = currentResponse.weather.firstOrNull()?.description ?: "Unknown",
            humidity = currentResponse.main.humidity,
            windSpeedMps = currentResponse.wind.speed,
            date = Instant.ofEpochSecond(currentResponse.dt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            latitude = currentResponse.coord?.lat,
            longitude = currentResponse.coord?.lon
        )

        // Group forecast by day and get daily highs/lows
        val dailyForecasts = forecastResponse.list
            .groupBy { item ->
                Instant.ofEpochSecond(item.dt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .entries
            .take(5)
            .map { (date, items) ->
                DailyForecast(
                    date = date,
                    highTempCelsius = items.maxOf { it.main.tempMax },
                    lowTempCelsius = items.minOf { it.main.tempMin },
                    condition = WeatherCondition.fromString(
                        items.firstOrNull()?.weather?.firstOrNull()?.main ?: "Unknown"
                    ),
                    conditionDescription = items.firstOrNull()?.weather?.firstOrNull()?.description ?: "Unknown"
                )
            }

        return WeatherData(current = current, forecast = dailyForecasts)
    }

    fun close() {
        client.close()
    }
}

class WeatherServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)
