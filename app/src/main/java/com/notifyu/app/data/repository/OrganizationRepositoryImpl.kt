package com.notifyu.app.data.repository

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.data.model.SelectedScreen
import com.notifyu.app.data.model.User
import com.notifyu.app.domain.repository.OrganizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

class OrganizationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
):OrganizationRepository {
    override suspend fun addOrganization(name: String, code: String): Result<String> {
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

    override suspend fun addMessage(
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


    override fun fetchOwnedOrganizations(onUpdate: (List<Organization>) -> Unit) {
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

    override fun fetchMessagesForOrganization(orgId: String, onUpdate: (List<Message>) -> Unit) {
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

    override suspend fun joinOrganizationByNameAndCode(name: String, code: String): Result<String> {
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

    override fun fetchMemberOrganizations(onUpdate: (List<Organization>) -> Unit) {
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

    override suspend fun fetchUsersByIds(userIds: List<String>): Result<List<User>> {
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


    override suspend fun removeMemberFromOrganization(
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



    override suspend fun updateOrganizationAvatarIndex(orgId: String, newAvatarIndex: Int): Result<String> {
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