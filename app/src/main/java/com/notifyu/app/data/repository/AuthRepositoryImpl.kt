package com.notifyu.app.data.repository

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.data.model.SelectedScreen
import com.notifyu.app.data.model.User
import com.notifyu.app.domain.repository.AuthRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
): AuthRepository {
    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User is null"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }




    override suspend fun sendEmailVerification(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val user = Firebase.auth.currentUser
                if (user == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }
                user.sendEmailVerification().await()
                Result.success("Verification email sent successfully")
            } catch (e: Exception) {
                Result.failure(Exception("Failed to send verification email"))
            }
        }
    }

    override suspend fun checkEmailVerification(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = Firebase.auth.currentUser
                if (user == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }
                user.reload().await()
                Result.success(user.isEmailVerified)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to check email verification"))
            }
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                Result.success("Login successful")
            } catch (e: Exception) {
                Result.failure(Exception("Login failed"))
            }
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                if (user == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }
                user.updatePassword(newPassword).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}


