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
 * OpenMeteo API response models
 */
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    @SerializedName("generationtime_ms") val generationTimeMs: Double,
    @SerializedName("utc_offset_seconds") val utcOffsetSeconds: Int,
    val timezone: String,
    @SerializedName("timezone_abbreviation") val timezoneAbbreviation: String,
    val current: CurrentData?,
    @SerializedName("current_units") val currentUnits: CurrentUnits?,
    val daily: DailyData?,
    @SerializedName("daily_units") val dailyUnits: DailyUnits?
)

data class CurrentData(
    val time: String,
    val interval: Int,
    @SerializedName("temperature_2m") val temperature2m: Double,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double
)

data class CurrentUnits(
    val time: String,
    val interval: String,
    @SerializedName("temperature_2m") val temperature2m: String,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: String,
    @SerializedName("weather_code") val weatherCode: String,
    @SerializedName("wind_speed_10m") val windSpeed10m: String
)

data class DailyData(
    val time: List<String>,
    @SerializedName("weather_code") val weatherCode: List<Int>,
    @SerializedName("temperature_2m_max") val temperature2mMax: List<Double>,
    @SerializedName("temperature_2m_min") val temperature2mMin: List<Double>
)

data class DailyUnits(
    val time: String,
    @SerializedName("weather_code") val weatherCode: String,
    @SerializedName("temperature_2m_max") val temperature2mMax: String,
    @SerializedName("temperature_2m_min") val temperature2mMin: String
)

/**
 * Geocoding API response for city name lookup
 */
data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val timezone: String?,
    val country: String?,
    @SerializedName("country_code") val countryCode: String?,
    val admin1: String?,
    val admin2: String?,
    val admin3: String?,
    val admin4: String?
)

/**
 * Service for interacting with OpenMeteo API
 * OpenMeteo is a free, open-source weather API that doesn't require API keys
 */
class OpenMeteoService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    private val forecastBaseUrl = "https://api.open-meteo.com/v1/forecast"
    private val geocodingBaseUrl = "https://geocoding-api.open-meteo.com/v1/search"

    /**
     * Fetch current weather by city name
     * First geocodes the city name to coordinates, then fetches weather
     */
    suspend fun getCurrentWeatherByCity(city: String): WeatherData {
        logger.info { "Fetching weather for city: $city" }

        return try {
            // Step 1: Geocode city name to coordinates
            val geocodingUrl = "$geocodingBaseUrl?name=$city&count=1&language=en&format=json"
            val geocodingResponse: GeocodingResponse = client.get(geocodingUrl).body()

            val location = geocodingResponse.results?.firstOrNull()
                ?: throw WeatherServiceException("City not found: $city")

            logger.info { "Geocoded $city to coordinates: (${location.latitude}, ${location.longitude})" }

            // Step 2: Fetch weather for coordinates
            getCurrentWeatherByCoordinates(location.latitude, location.longitude, location.name)
        } catch (e: WeatherServiceException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Error fetching weather for city: $city" }
            throw WeatherServiceException("Failed to fetch weather for $city: ${e.message}", e)
        }
    }

    /**
     * Fetch current weather by coordinates
     */
    suspend fun getCurrentWeatherByCoordinates(lat: Double, lon: Double, cityName: String? = null): WeatherData {
        logger.info { "Fetching weather for coordinates: ($lat, $lon)" }

        return try {
            val weatherUrl = buildString {
                append(forecastBaseUrl)
                append("?latitude=$lat")
                append("&longitude=$lon")
                append("&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m")
                append("&daily=weather_code,temperature_2m_max,temperature_2m_min")
                append("&timezone=auto")
                append("&forecast_days=7")
            }

            val response: OpenMeteoResponse = client.get(weatherUrl).body()

            mapToWeatherData(response, cityName)
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
     * Map OpenMeteo API response to domain model
     */
    private fun mapToWeatherData(response: OpenMeteoResponse, cityName: String?): WeatherData {
        val currentData = response.current
            ?: throw WeatherServiceException("No current weather data available")

        val dailyData = response.daily
            ?: throw WeatherServiceException("No forecast data available")

        // Determine city name (use provided name or format from coordinates)
        val city = cityName ?: formatCityName(response.latitude, response.longitude)

        // Parse current weather
        val currentWeatherDate = parseDate(currentData.time)
        val condition = weatherCodeToCondition(currentData.weatherCode)

        val current = CurrentWeather(
            city = city,
            temperatureCelsius = currentData.temperature2m,
            condition = condition,
            conditionDescription = weatherCodeToDescription(currentData.weatherCode),
            humidity = currentData.relativeHumidity2m,
            windSpeedMps = currentData.windSpeed10m / 3.6, // OpenMeteo returns km/h, convert to m/s
            date = currentWeatherDate,
            latitude = response.latitude,
            longitude = response.longitude
        )

        // Parse daily forecasts (skip today, take next 5 days)
        val forecasts = dailyData.time.drop(1).take(5).mapIndexed { index, dateStr ->
            val actualIndex = index + 1 // Account for dropping first element
            DailyForecast(
                date = LocalDate.parse(dateStr),
                highTempCelsius = dailyData.temperature2mMax[actualIndex],
                lowTempCelsius = dailyData.temperature2mMin[actualIndex],
                condition = weatherCodeToCondition(dailyData.weatherCode[actualIndex]),
                conditionDescription = weatherCodeToDescription(dailyData.weatherCode[actualIndex])
            )
        }

        return WeatherData(current = current, forecast = forecasts)
    }

    /**
     * Parse ISO 8601 date string to LocalDate
     */
    private fun parseDate(dateTimeStr: String): LocalDate {
        return try {
            val instant = Instant.parse(dateTimeStr)
            instant.atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    /**
     * Format city name from coordinates
     */
    private fun formatCityName(lat: Double, lon: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        return String.format("%.2f°%s, %.2f°%s", kotlin.math.abs(lat), latDir, kotlin.math.abs(lon), lonDir)
    }

    /**
     * Convert WMO weather code to WeatherCondition enum
     * Based on WMO Code Table 4677
     */
    private fun weatherCodeToCondition(code: Int): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition.CLEAR
            1 -> WeatherCondition.FEW_CLOUDS
            2 -> WeatherCondition.SCATTERED_CLOUDS
            3 -> WeatherCondition.BROKEN_CLOUDS
            45, 48 -> WeatherCondition.MIST
            51, 53, 55, 56, 57 -> WeatherCondition.RAIN // Drizzle
            61, 63, 65, 66, 67 -> WeatherCondition.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
            80, 81, 82 -> WeatherCondition.SHOWER_RAIN
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.UNKNOWN
        }
    }

    /**
     * Convert WMO weather code to human-readable description
     */
    private fun weatherCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45 -> "Foggy"
            48 -> "Depositing rime fog"
            51 -> "Light drizzle"
            53 -> "Moderate drizzle"
            55 -> "Dense drizzle"
            56 -> "Light freezing drizzle"
            57 -> "Dense freezing drizzle"
            61 -> "Slight rain"
            63 -> "Moderate rain"
            65 -> "Heavy rain"
            66 -> "Light freezing rain"
            67 -> "Heavy freezing rain"
            71 -> "Slight snow"
            73 -> "Moderate snow"
            75 -> "Heavy snow"
            77 -> "Snow grains"
            80 -> "Slight rain showers"
            81 -> "Moderate rain showers"
            82 -> "Violent rain showers"
            85 -> "Slight snow showers"
            86 -> "Heavy snow showers"
            95 -> "Thunderstorm"
            96 -> "Thunderstorm with slight hail"
            99 -> "Thunderstorm with heavy hail"
            else -> "Unknown"
        }
    }

    fun close() {
        client.close()
    }
}

class WeatherServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)
