package com.notifyu.app.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.data.model.LastMessage
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.data.model.User
import com.notifyu.app.domain.repository.OrganizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.String
import kotlin.collections.filterIsInstance

class OrganizationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
) : OrganizationRepository {
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
                    "message" to emptyList<Map<String, Any>>(),
                    "lastMessage" to mapOf(
                        "content" to "",
                        "senderId" to "",
                        "timestamp" to 0L,
                        "seenBy" to emptyList<String>()
                    )
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
                    "timestamp" to System.currentTimeMillis(),
                )

                firestore.collection("organizations")
                    .document(currentOrgId)
                    .update("message", FieldValue.arrayUnion(message))
                    .await()

                addUpdateLastMessage(
                    content,
                    senderId,
                    currentOrgId,
                )

                Result.success("Message added successfully")

            } catch (e: Exception) {
                Result.failure(Exception("Failed to add message"))
            }
        }
    }

    private suspend fun addUpdateLastMessage(
        content: String,
        senderId: String,
        currentOrgId: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (currentOrgId.isBlank()) {
                    return@withContext Result.failure(Exception("Organization ID is empty"))
                }

                val lastMessage = mapOf(
                    "content" to content,
                    "senderId" to senderId,
                    "timestamp" to System.currentTimeMillis(),
                    "seenBy" to emptyList<String>()
                )

                firestore.collection("organizations")
                    .document(currentOrgId)
                    .update("lastMessage", lastMessage)
                    .await()

                Result.success("Last message updated successfully")
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update last message"))
            }
        }
    }

    override suspend fun updateSeenByForLastMessage(
        currentOrgId: String,
        currentUserUid: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = firestore.collection("organizations").document(currentOrgId)
                val snapshot = docRef.get().await()

                if (!snapshot.exists()) {
                    return@withContext Result.failure(Exception("Organization not found"))
                }

                // Check owner
                val owner = snapshot.getString("owner") ?: ""
                if (owner == currentUserUid) {
                    return@withContext Result.success("Owner does not count as seen")
                }

                // Now update seenBy list on lastMessage
                val lastMessageMap = snapshot.get("lastMessage") as? Map<*, *> ?: run {
                    return@withContext Result.failure(Exception("Last message not found"))
                }

                val seenBy = (lastMessageMap["seenBy"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList()

                if (currentUserUid in seenBy) {
                    return@withContext Result.success("User already in seenBy list")
                }

                val updatedSeenBy = seenBy + currentUserUid
                val updatedLastMessage = lastMessageMap.toMutableMap()
                updatedLastMessage["seenBy"] = updatedSeenBy

                docRef.update("lastMessage", updatedLastMessage).await()

                Result.success("SeenBy list updated successfully")
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update seenBy list: ${e.message}"))
            }
        }
    }



//    override fun fetchOwnedOrganizations(onUpdate: (List<Organization>) -> Unit) {
//        try {
//            val user = firebaseAuth.currentUser ?: run {
//                onUpdate(emptyList())
//                return
//            }
//
//            firestore.collection("organizations")
//                .whereEqualTo("owner", user.uid)
//                .addSnapshotListener { snapshot, error ->
//                    try {
//                        if (error != null) {
//                            onUpdate(emptyList())
//                            return@addSnapshotListener
//                        }
//
//                        if (snapshot != null && !snapshot.isEmpty) {
//                            val owned = snapshot.documents.mapNotNull { doc ->
//                                try {
//                                    val lastMessageMap = doc.get("lastMessage") as? Map<*, *>
//                                    val lastMessage = lastMessageMap?.let {
//                                        LastMessage(
//                                            content = it["content"] as? String ?: "",
//                                            senderId = it["senderId"] as? String ?: "",
//                                            timestamp = (it["timestamp"] as? Number)?.toLong()
//                                                ?: 0L,
//                                            seenBy = (it["seenBy"] as? List<*>)?.filterIsInstance<String>()
//                                                ?: emptyList()
//                                        )
//                                    }
//                                    val messagesList =
//                                        (snapshot.get("message") as? List<*>)?.mapNotNull { msg ->
//                                            (msg as? Map<*, *>)?.let { msgMap ->
//                                                Message(
//                                                    content = msgMap["content"] as? String ?: "",
//                                                    senderId = msgMap["senderId"] as? String ?: "",
//                                                    timestamp = (msgMap["timestamp"] as? Number)?.toLong()
//                                                        ?: 0L,
//                                                )
//                                            }
//                                        } ?: emptyList()
//
//                                    Organization(
//                                        id = doc.id,
//                                        name = doc.getString("name") ?: "",
//                                        code = doc.getString("code") ?: "",
//                                        owner = doc.getString("owner") ?: "",
//                                        avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
//                                        members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
//                                            ?: emptyList(),
//                                        messages = emptyList(), // load messages separately if needed
//                                        lastMessage = lastMessage
//                                    )
//                                } catch (e: Exception) {
//                                    null
//                                }
//                            }
//
//                            onUpdate(owned)
//                        } else {
//                            onUpdate(emptyList())
//                        }
//                    } catch (e: Exception) {
//                        onUpdate(emptyList())
//                    }
//                }
//
//        } catch (e: Exception) {
//            onUpdate(emptyList())
//        }
//    }

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
                                    val lastMessageMap = doc.get("lastMessage") as? Map<*, *>
                                    val lastMessage = lastMessageMap?.let {
                                        LastMessage(
                                            content = it["content"] as? String ?: "",
                                            senderId = it["senderId"] as? String ?: "",
                                            timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L,
                                            seenBy = (it["seenBy"] as? List<*>)?.filterIsInstance<String>()
                                                ?: emptyList()
                                        )
                                    }

                                    val messagesList = (doc.get("message") as? List<*>)?.mapNotNull { msg ->
                                        (msg as? Map<*, *>)?.let { msgMap ->
                                            Message(
                                                content = msgMap["content"] as? String ?: "",
                                                senderId = msgMap["senderId"] as? String ?: "",
                                                timestamp = (msgMap["timestamp"] as? Number)?.toLong()
                                                    ?: 0L
                                            )
                                        }
                                    } ?: emptyList()

                                    Organization(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        code = doc.getString("code") ?: "",
                                        owner = doc.getString("owner") ?: "",
                                        avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
                                        members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
                                            ?: emptyList(),
                                        messages = messagesList,
                                        lastMessage = lastMessage
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
                                        timestamp = (msgMap["timestamp"] as? Number)?.toLong()
                                            ?: 0L,
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

                        val memberOf = snapshot.documents.mapNotNull { doc ->
                            try {
                                val lastMessageMap = doc.get("lastMessage") as? Map<*, *>
                                val lastMessage = lastMessageMap?.let {
                                    LastMessage(
                                        content = it["content"] as? String ?: "",
                                        senderId = it["senderId"] as? String ?: "",
                                        timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L,
                                        seenBy = (it["seenBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                    )
                                }

                                val messagesList = (doc.get("message") as? List<*>)?.mapNotNull { msg ->
                                    (msg as? Map<*, *>)?.let { msgMap ->
                                        Message(
                                            content = msgMap["content"] as? String ?: "",
                                            senderId = msgMap["senderId"] as? String ?: "",
                                            timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: 0L
                                        )
                                    }
                                } ?: emptyList()

                                Organization(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "",
                                    code = doc.getString("code") ?: "",
                                    owner = doc.getString("owner") ?: "",
                                    avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
                                    members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
                                        ?: emptyList(),
                                    messages = messagesList,
                                    lastMessage = lastMessage
                                )
                            } catch (e: Exception) {
                                null
                            }
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
        uidToRemove: String,
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


    override suspend fun updateOrganizationAvatarIndex(
        orgId: String,
        newAvatarIndex: Int,
    ): Result<String> {
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