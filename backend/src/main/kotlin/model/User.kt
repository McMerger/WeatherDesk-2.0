package model

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val lastSearchedCity: String?,
    val lastSearchedLatitude: Double?,
    val lastSearchedLongitude: Double?,
    val preferredTempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val preferredWindUnit: WindSpeedUnit = WindSpeedUnit.KILOMETERS_PER_HOUR
) {
    fun toModel(): User = User(
        lastSearchedCity,
        lastSearchedLatitude,
        lastSearchedLongitude,
        preferredTempUnit,
        preferredWindUnit
    )
}

data class User(
    val lastSearchedCity: String?,
    val lastSearchedLatitude: Double?,
    val lastSearchedLongitude: Double?,
    val preferredTempUnit: TemperatureUnit,
    val preferredWindUnit: WindSpeedUnit
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "lastSearchedCity" to lastSearchedCity,
        "lastSearchedLatitude" to lastSearchedLatitude,
        "lastSearchedLongitude" to lastSearchedLongitude,
        "preferredTempUnit" to preferredTempUnit.name,
        "preferredWindUnit" to preferredWindUnit.name
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            map["lastSearchedCity"] as? String,
            (map["lastSearchedLatitude"] as? Number)?.toDouble(),
            (map["lastSearchedLongitude"] as? Number)?.toDouble(),
            TemperatureUnit.valueOf(map["preferredTempUnit"] as? String ?: "CELSIUS"),
            WindSpeedUnit.valueOf(map["preferredWindUnit"] as? String ?: "KILOMETERS_PER_HOUR")
        )
    }
}

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class WindSpeedUnit { KILOMETERS_PER_HOUR, MILES_PER_HOUR }