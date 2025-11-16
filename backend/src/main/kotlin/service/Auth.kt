package service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthService {

    suspend fun verifyIdToken(idToken: String): String = withContext(Dispatchers.IO) {
        try {
            val decodedToken: FirebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            decodedToken.uid
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Firebase ID token.")
        }
    }
}

