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
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
): AuthRepository {
    override suspend fun signUp(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    createUser(user)
                } else {
                    Result.failure(Exception("User is null"))
                }
            } catch (e: Exception) {
                Log.e("SignUp", "Failed: ${e.message}")
                Result.failure(e)
            }
        }

    private suspend fun createUser(user: FirebaseUser): Result<String> =
        withContext(Dispatchers.IO) {
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

    suspend fun addOrganization(name: String, code: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                if (user == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                // Check if organization already exists
                val existing = firestore.collection("organizations")
                    .whereEqualTo("name", name)
                    .get()
                    .await()

                if (!existing.isEmpty) {
                    return@withContext Result.failure(Exception("Organization with this name already exists"))
                }

                val id = firestore.collection("organizations").document().id
                val newOrg = hashMapOf(
                    "id" to id,
                    "name" to name,
                    "code" to code,
                    "owner" to user.uid,
                    "avatarIndex" to 0,
                    "members" to emptyList<String>(),
                    "message" to emptyList<Map<String, Any>>()
                )

                firestore.collection("organizations")
                    .document(id)
                    .set(newOrg)
                    .await()

                return@withContext Result.success("Organization added successfully")
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Error adding organization "))
            }
        }
    }

    suspend fun addMessage(
        content: String,
        senderId: String,
        currentOrgId: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (currentOrgId.isBlank()) {
                    return@withContext Result.failure(Exception("Organization ID is empty"))
                }

                val message = mapOf(
                    "content" to content,
                    "senderId" to senderId,
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("organizations")
                    .document(currentOrgId)
                    .update("message", FieldValue.arrayUnion(message))
                    .await()

                Result.success("Message added successfully")

            } catch (e: Exception) {
                Result.failure(Exception("Failed to add message"))
            }
        }
    }


    fun fetchOwnedOrganizations(onUpdate: (List<Organization>) -> Unit) {
        try {
            val user = firebaseAuth.currentUser ?: run {
                onUpdate(emptyList())
                return
            }

            firestore.collection("organizations")
                .whereEqualTo("owner", user.uid)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null) {
                            onUpdate(emptyList())
                            return@addSnapshotListener
                        }

                        if (snapshot != null && !snapshot.isEmpty) {
                            val owned = snapshot.documents.mapNotNull { doc ->
                                try {
                                    Organization(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        code = doc.getString("code") ?: "",
                                        owner = doc.getString("owner") ?: "",
                                        avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
                                        members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
                                            ?: emptyList(),
                                        messages = emptyList()
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            onUpdate(owned)
                        } else {
                            onUpdate(emptyList())
                        }
                    } catch (e: Exception) {
                        onUpdate(emptyList())
                    }
                }

        } catch (e: Exception) {
            onUpdate(emptyList())
        }
    }

    fun fetchMessagesForOrganization(orgId: String, onUpdate: (List<Message>) -> Unit) {
        try {
            firestore.collection("organizations")
                .document(orgId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }

                    try {
                        val messagesList =
                            (snapshot.get("message") as? List<*>)?.mapNotNull { msg ->
                                (msg as? Map<*, *>)?.let { msgMap ->
                                    Message(
                                        content = msgMap["content"] as? String ?: "",
                                        senderId = msgMap["senderId"] as? String ?: "",
                                        timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: 0L
                                    )
                                }
                            } ?: emptyList()
                        onUpdate(messagesList)
                    } catch (e: Exception) {
                        onUpdate(emptyList())
                    }
                }
        } catch (e: Exception) {
            onUpdate(emptyList())
        }
    }

    suspend fun joinOrganizationByNameAndCode(name: String, code: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("User not logged in"))

            try {
                val querySnapshot = firestore.collection("organizations")
                    .whereEqualTo("name", name)
                    .whereEqualTo("code", code)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    return@withContext Result.failure(Exception("No matching organization found"))
                }

                val document = querySnapshot.documents.first()
                val ownerId = document.getString("owner")

                if (ownerId == currentUserId) {
                    return@withContext Result.failure(Exception("You are already the owner of this organization"))
                }

                val orgRef = document.reference
                orgRef.update("members", FieldValue.arrayUnion(currentUserId)).await()

                Result.success("Joined organization successfully")
            } catch (e: Exception) {
                Result.failure(Exception("Error joining organization "))
            }
        }
    }

    fun fetchMemberOrganizations(onUpdate: (List<Organization>) -> Unit) {
        try {
            val currentUid = firebaseAuth.currentUser?.uid ?: run {
                onUpdate(emptyList())
                return
            }

            firestore.collection("organizations")
                .whereArrayContains("members", currentUid)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null || snapshot == null) {
                            onUpdate(emptyList())
                            return@addSnapshotListener
                        }

                        val memberOf = snapshot.documents.map { doc ->
                            Organization(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                code = doc.getString("code") ?: "",
                                owner = doc.getString("owner") ?: "",
                                avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
                                members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
                                    ?: emptyList()
                            )
                        }
                        onUpdate(memberOf)
                    } catch (e: Exception) {
                        onUpdate(emptyList())
                    }
                }

        } catch (e: Exception) {
            onUpdate(emptyList())
        }
    }

    suspend fun syncFcmTokenIfChanged(): Result<String> {
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

    suspend fun sendFcmPushNotification(
        context: Context,
        targetTokens: List<String>,
        title: String,
        body: String,
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

    suspend fun fetchUsersByIds(userIds: List<String>): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                if (userIds.isEmpty()) return@withContext Result.success(emptyList())

                val chunks = userIds.chunked(10)
                val allUsers = mutableListOf<User>()

                for (chunk in chunks) {
                    val snapshot = firestore.collection("users")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()

                    val users = snapshot.documents.mapNotNull { doc ->
                        val email = doc.getString("email")
                        val fcmToken = doc.getString("fcmToken")
                        val uid = doc.id
                        if (email != null && fcmToken != null) {
                            User(uid, email, fcmToken)
                        } else null
                    }

                    allUsers.addAll(users)
                }

                Result.success(allUsers)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    suspend fun removeMemberFromOrganization(
        organizationId: String,
        uidToRemove: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (organizationId.isBlank()) {
                    return@withContext Result.failure(Exception("Organization ID is empty"))
                }

                val orgDoc = firestore.collection("organizations").document(organizationId)

                orgDoc.update("members", FieldValue.arrayRemove(uidToRemove)).await()

                Result.success("Removed successfully")
            } catch (e: Exception) {
                Result.failure(Exception("Failed to remove"))
            }
        }
    }

    suspend fun updateSelectedScreen(uid: String, selectedScreen: SelectedScreen): Result<String> {
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



    fun fetchSelectedScreenForCurrentUser(onResult: (String?) -> Unit) {
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

    suspend fun updateOrganizationAvatarIndex(orgId: String, newAvatarIndex: Int): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                firestore.collection("organizations")
                    .document(orgId)
                    .update("avatarIndex", newAvatarIndex)
                    .await()

                Result.success("Avatar updated successfully")
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update avatar index"))
            }
        }
    }



}


