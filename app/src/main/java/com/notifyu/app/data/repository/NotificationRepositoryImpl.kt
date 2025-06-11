package com.notifyu.app.data.repository

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

// Implementation of NotificationRepository interface
class NotificationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Firebase Authentication instance to get the current user
    private val firestore: FirebaseFirestore, // Firestore database instance
    private val firebaseMessaging: FirebaseMessaging, // Firebase Messaging instance for FCM tokens
) : NotificationRepository {

    // Function to sync the FCM token if it has changed
    override suspend fun syncFcmTokenIfChanged(): Result<String> {
        return withContext(Dispatchers.IO) { // Run on IO dispatcher for background work
            try {
                // Get current user's UID from Firebase Auth
                val userUid = firebaseAuth.currentUser?.uid
                    ?: return@withContext Result.failure(Exception("User not found")) // If user is null, return failure

                // Fetch the latest FCM token from Firebase Messaging
                val token = firebaseMessaging.token.await()

                // Reference to user's document in Firestore
                val userDocRef = firestore.collection("users").document(userUid)
                val snapshot = userDocRef.get().await() // Fetch the document

                // Retrieve previously saved token from Firestore
                val savedToken = snapshot.getString("fcmToken")

                // Compare with current token
                if (savedToken != token) {
                    // Update the token in Firestore if changed
                    userDocRef.update("fcmToken", token).await()
                    Result.success("Token synced successfully") // Success message
                } else {
                    // Token already up to date
                    Result.success("Token already up to date")
                }
            } catch (e: Exception) {
                // Catch and return any exception that occurred
                Result.failure(e)
            }
        }
    }

    // Function to send push notifications using Firebase Cloud Messaging (FCM) HTTP v1 API
    override suspend fun sendFcmPushNotification(
        context: Context,
        targetTokens: List<String>, // List of target FCM device tokens
        title: String, // Notification title
        body: String, // Notification body
        orgId: String, // Organization ID to send as custom data
        orgName: String // Organization name to send as custom data
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Load the service account credentials from the assets folder
                val googleCredentials =
                    context.assets.open("service-account.json").use { inputStream ->
                        GoogleCredentials.fromStream(inputStream)
                            .createScoped("https://www.googleapis.com/auth/firebase.messaging") // Required scope for FCM
                    }

                // Refresh the token if expired
                googleCredentials.refreshIfExpired()
                val accessToken = googleCredentials.accessToken.tokenValue // Access token for authorization

                val client = OkHttpClient() // OkHttp client to make HTTP requests
                val mediaType = "application/json; charset=utf-8".toMediaType() // Define media type for JSON body

                // Loop through all target device tokens
                for (token in targetTokens) {
                    // Create the JSON body for the FCM request
                    val json = JSONObject().apply {
                        put("message", JSONObject().apply {
                            put("token", token) // Target device token
                            put("notification", JSONObject().apply {
                                put("title", title) // Notification title
                                put("body", body) // Notification body
                            })
                            put("data", JSONObject().apply {
                                put("orgId", orgId) // Custom data field: organization ID
                                put("orgName", orgName) // Custom data field: organization name
                            })
                        })
                    }

                    // Convert JSON object to request body
                    val requestBody = json.toString().toRequestBody(mediaType)

                    // Build the HTTP POST request to FCM endpoint
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/notifyu-82ee4/messages:send") // Note: hardcoded project ID
                        .addHeader("Authorization", "Bearer $accessToken") // Bearer token for authentication
                        .addHeader("Content-Type", "application/json; UTF-8") // Content type header
                        .post(requestBody) // Attach request body
                        .build()

                    // Execute the request
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() // Get response body as string

                    // Log the result for each token
                    Log.d("FCM", "Token: $token → ${response.code} - $responseBody")

                    // Check if response was not successful
                    if (!response.isSuccessful) {
                        Log.e("FCM", "Failed to send FCM to $token → $responseBody")
                    }
                }
                Result.success(Unit) // If all went well, return success
            } catch (e: Exception) {
                // Catch and log any exception
                Log.e("FCM", "Push notification error", e)
                Result.failure(e)
            }
        }
    }
}
