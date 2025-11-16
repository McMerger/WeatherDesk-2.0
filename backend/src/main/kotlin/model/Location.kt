package model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "name" to name,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Location = Location(
            latitude = (map["latitude"] as Number).toDouble(),
            longitude = (map["longitude"] as Number).toDouble(),
            name = map["name"] as String,
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}