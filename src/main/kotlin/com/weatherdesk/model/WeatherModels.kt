package com.weatherdesk.model

import java.time.LocalDate

/**
 * Temperature unit enumeration
 */
enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F"),
    KELVIN("K");

    fun convertFromCelsius(celsius: Double): Double = when (this) {
        CELSIUS -> celsius
        FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
        KELVIN -> celsius + 273.15
    }

    fun convertToCelsius(value: Double): Double = when (this) {
        CELSIUS -> value
        FAHRENHEIT -> (value - 32.0) * 5.0 / 9.0
        KELVIN -> value - 273.15
    }
}

/**
 * Wind speed unit enumeration
 */
enum class WindSpeedUnit(val symbol: String) {
    KILOMETERS_PER_HOUR("km/h"),
    MILES_PER_HOUR("mph"),
    METERS_PER_SECOND("m/s");

    fun convertFromMetersPerSecond(mps: Double): Double = when (this) {
        METERS_PER_SECOND -> mps
        KILOMETERS_PER_HOUR -> mps * 3.6
        MILES_PER_HOUR -> mps * 2.23694
    }
}

/**
 * Weather condition types matching OpenWeatherMap API
 */
enum class WeatherCondition {
    CLEAR,
    CLOUDS,
    FEW_CLOUDS,
    SCATTERED_CLOUDS,
    BROKEN_CLOUDS,
    SHOWER_RAIN,
    RAIN,
    THUNDERSTORM,
    SNOW,
    MIST,
    UNKNOWN;

    companion object {
        fun fromString(condition: String): WeatherCondition {
            return when (condition.lowercase()) {
                "clear" -> CLEAR
                "clouds" -> CLOUDS
                "few clouds" -> FEW_CLOUDS
                "scattered clouds" -> SCATTERED_CLOUDS
                "broken clouds" -> BROKEN_CLOUDS
                "shower rain" -> SHOWER_RAIN
                "rain", "light rain", "moderate rain", "heavy intensity rain" -> RAIN
                "thunderstorm" -> THUNDERSTORM
                "snow" -> SNOW
                "mist", "fog", "haze" -> MIST
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Current weather data
 */
data class CurrentWeather(
    val city: String,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val conditionDescription: String,
    val humidity: Int,
    val windSpeedMps: Double,
    val date: LocalDate,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun getTemperature(unit: TemperatureUnit): Double {
        return unit.convertFromCelsius(temperatureCelsius)
    }

    fun getWindSpeed(unit: WindSpeedUnit): Double {
        return unit.convertFromMetersPerSecond(windSpeedMps)
    }

    fun getFormattedTemperature(unit: TemperatureUnit): String {
        return String.format("%.1f%s", getTemperature(unit), unit.symbol)
    }

    fun getFormattedWindSpeed(unit: WindSpeedUnit): String {
        return String.format("%.1f %s", getWindSpeed(unit), unit.symbol)
    }
}

/**
 * Daily forecast data
 */
data class DailyForecast(
    val date: LocalDate,
    val highTempCelsius: Double,
    val lowTempCelsius: Double,
    val condition: WeatherCondition,
    val conditionDescription: String
) {
    fun getHighTemp(unit: TemperatureUnit): Double {
        return unit.convertFromCelsius(highTempCelsius)
    }

    fun getLowTemp(unit: TemperatureUnit): Double {
        return unit.convertFromCelsius(lowTempCelsius)
    }

    fun getFormattedTemps(unit: TemperatureUnit): String {
        return String.format("%.0f° / %.0f°", getHighTemp(unit), getLowTemp(unit))
    }
}

/**
 * Complete weather data including current and forecast
 */
data class WeatherData(
    val current: CurrentWeather,
    val forecast: List<DailyForecast>
)

/**
 * User preferences
 */
data class UserPreferences(
    val lastSearchedCity: String? = null,
    val lastSearchedLatitude: Double? = null,
    val lastSearchedLongitude: Double? = null,
    val preferredTempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val preferredWindUnit: WindSpeedUnit = WindSpeedUnit.KILOMETERS_PER_HOUR
)

/**
 * Forecast rating
 */
data class ForecastRating(
    val city: String,
    val rating: Int,
    val date: LocalDate,
    val userId: String = "default_user"
)

/**
 * Location input type
 */
sealed class LocationInput {
    data class City(val name: String) : LocationInput()
    data class Coordinates(val latitude: Double, val longitude: Double) : LocationInput() {
        init {
            require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
            require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
        }
    }
}

/**
 * Result wrapper for operations that can fail
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
