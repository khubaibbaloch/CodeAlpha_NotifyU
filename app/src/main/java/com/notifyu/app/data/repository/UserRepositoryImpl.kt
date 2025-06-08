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

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
):UserRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun createUser(user: FirebaseUser): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "fcmToken" to token,
                )
                Firebase.firestore.collection("users").document(user.uid).set(userData).await()
                Result.success("Sign Up Successful")
            } catch (e: Exception) {
                Log.e("CreateUser", "Error: ${e.message}")
                Result.failure(e)
            }
        }
    }



}