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

class NotificationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
): NotificationRepository {

    override suspend fun syncFcmTokenIfChanged(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userUid = firebaseAuth.currentUser?.uid
                    ?: return@withContext Result.failure(Exception("User not found"))

                val token = firebaseMessaging.token.await()

                val userDocRef = firestore.collection("users").document(userUid)
                val snapshot = userDocRef.get().await()

                val savedToken = snapshot.getString("fcmToken")
                if (savedToken != token) {
                    userDocRef.update("fcmToken", token).await()
                    Result.success("Token synced successfully")
                } else {
                    Result.success("Token already up to date")
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun sendFcmPushNotification(
        context: Context,
        targetTokens: List<String>,
        title: String,
        body: String,
        orgId: String,
        orgName: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {

                val googleCredentials =
                    context.assets.open("service-account.json").use { inputStream ->
                        GoogleCredentials.fromStream(inputStream)
                            .createScoped("https://www.googleapis.com/auth/firebase.messaging")
                    }

                googleCredentials.refreshIfExpired()
                val accessToken = googleCredentials.accessToken.tokenValue


                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaType()

                for (token in targetTokens) {
                    val json = JSONObject().apply {
                        put("message", JSONObject().apply {
                            put("token", token)
                            put("notification", JSONObject().apply {
                                put("title", title)
                                put("body", body)
                            })
                            put("data", JSONObject().apply {
                                put("orgId", orgId)
                                put("orgName", orgName)
                            })
                        })
                    }


                    val requestBody = json.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/notifyu-82ee4/messages:send")
                        .addHeader("Authorization", "Bearer $accessToken")
                        .addHeader("Content-Type", "application/json; UTF-8")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    Log.d("FCM", "Token: $token → ${response.code} - $responseBody")

                    if (!response.isSuccessful) {
                        Log.e("FCM", "Failed to send FCM to $token → $responseBody")
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("FCM", "Push notification error", e)
                Result.failure(e)
            }
        }

    }

}