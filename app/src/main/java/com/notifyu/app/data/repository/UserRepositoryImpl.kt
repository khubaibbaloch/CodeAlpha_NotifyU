package com.notifyu.app.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Declares a class `UserRepositoryImpl` that implements the `UserRepository` interface
// It uses constructor injection (via @Inject) to receive instances of FirebaseAuth and FirebaseFirestore
class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Firebase authentication instance
    private val firestore: FirebaseFirestore // Firestore database instance (currently unused directly in this class)
) : UserRepository {

    // Suspended function to create a user entry in Firestore
    override suspend fun createUser(user: FirebaseUser): Result<String> {
        return withContext(Dispatchers.IO) { // Switch to IO dispatcher for network/database operations
            try {
                // Fetches the current device's FCM (Firebase Cloud Messaging) token asynchronously
                val token = FirebaseMessaging.getInstance().token.await()

                // Prepares user data as a HashMap to store in Firestore
                val userData = hashMapOf(
                    "uid" to user.uid,          // User ID from FirebaseAuth
                    "email" to user.email,      // User's email
                    "fcmToken" to token         // Firebase Cloud Messaging token
                )

                // Saves the user data in Firestore under the "users" collection using the UID as the document ID
                Firebase.firestore.collection("users").document(user.uid).set(userData).await()

                // Returns a success result with a custom message
                Result.success("Sign Up Successful")
            } catch (e: Exception) {
                // Logs the error with the tag "CreateUser" and returns a failure result with the exception
                Log.e("CreateUser", "Error: ${e.message}")
                Result.failure(e)
            }
        }
    }
}
