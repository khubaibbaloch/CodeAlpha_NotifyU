package com.notifyu.app.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.data.model.SelectedScreen
import com.notifyu.app.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
):UserRepository {

    override suspend fun createUser(user: FirebaseUser): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "fcmToken" to token,
                    "avatarIndex" to 0,
                    "selectedScreen" to SelectedScreen.None.value
                )
                Firebase.firestore.collection("users").document(user.uid).set(userData).await()
                Result.success("Sign Up Successful")
            } catch (e: Exception) {
                Log.e("CreateUser", "Error: ${e.message}")
                Result.failure(e)
            }
        }
    }
    override suspend fun updateSelectedScreen(uid: String, selectedScreen: SelectedScreen): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val screenValue = selectedScreen.value
                firestore.collection("users").document(uid)
                    .update("selectedScreen", screenValue)
                    .await()
                Log.d("Firestore", "selectedScreen updated to $screenValue")
                Result.success("selectedScreen updated")
            } catch (e: Exception) {
                Log.e("Firestore", "Failed to update selectedScreen", e)
                Result.failure(Exception("Failed to update selectedScreen"))
            }
        }
    }



    override fun fetchSelectedScreenForCurrentUser(onResult: (String?) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            onResult(null)
            return
        }

        firestore.collection("users").document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    onResult(null)
                    return@addSnapshotListener
                }

                val selectedScreen = snapshot.getString("selectedScreen")
                onResult(selectedScreen)
            }
    }
}