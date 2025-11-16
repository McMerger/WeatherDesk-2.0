package service

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Location

object FirebaseService {
    private val firestore: Firestore = FirestoreClient.getFirestore()

    suspend fun saveLastLocation(userId: String, location: Location) {
        withContext(Dispatchers.IO) {
            firestore.collection("users")
                .document(userId)
                .set(mapOf("lastLocation" to location.toMap()),
                    SetOptions.merge())
                .get()
        }
    }

    suspend fun addToSavedLocations(userId: String, location: Location) {
        withContext(Dispatchers.IO) {
            firestore.collection("users").document(userId)
                .update("savedLocations",
                    FieldValue.arrayUnion(location.toMap()))
                .get()
        }
    }

    suspend fun getLastLocation(userId: String): Location? {
        return withContext(Dispatchers.IO) {
            val doc = firestore.collection("users").document(userId).get().get()
            val map = doc.get("lastLocation") as? Map<*, *> ?: return@withContext null

            Location.fromMap(map as Map<String, Any?>)
        }
    }

    suspend fun getSavedLocations(userId: String): List<Location> {
        return withContext(Dispatchers.IO) {
            val doc = firestore.collection("users").document(userId).get().get()
            val list = doc.get("savedLocations") as? List<Map<String, Any?>> ?: return@withContext emptyList()

            list.map { Location.fromMap(it) }
        }
    }

    suspend fun removeSavedLocation(userId: String, locationName: String) {
        withContext(Dispatchers.IO) {
            val reference = firestore.collection("users").document(userId)
            val doc = reference.get().get()
            val savedList = doc.get("savedLocations") as? List<Map<String, Any?>> ?: emptyList()

            val filtered = savedList.filter { it["name"] != locationName }

            reference.update("savedLocations", filtered).get()
        }
    }
}