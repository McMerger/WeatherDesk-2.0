package model

import kotlinx.serialization.Serializable

@Serializable
data class ForecastRating(
    val id: String? = null,
    val userId: String,
    val city: String,
    val rating: Int,
    val date: String = java.time.LocalDate.now().toString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "city" to city,
        "rating" to rating,
        "date" to date,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): ForecastRating = ForecastRating(
            userId = map["userId"] as String,
            city = map["city"] as String,
            rating = (map["rating"] as Number).toInt(),
            date = map["date"] as? String ?: java.time.LocalDate.now().toString(),
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

