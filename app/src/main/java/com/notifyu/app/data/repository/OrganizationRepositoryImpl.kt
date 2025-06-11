package com.notifyu.app.data.repository


import android.util.Log
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

// Implementation of OrganizationRepository using Firebase services
class OrganizationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Handles Firebase user authentication
    private val firestore: FirebaseFirestore, // Firestore database instance
    private val firebaseMessaging: FirebaseMessaging, // Firebase Cloud Messaging instance (currently unused in this code)
) : OrganizationRepository {

    // Function to add a new organization to Firestore
    override suspend fun addOrganization(name: String, code: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser // Get currently logged-in user
                if (user == null) {
                    // Return error if user is not logged in
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                // Query to check if an organization with the same name already exists
                val existing = firestore.collection("organizations")
                    .whereEqualTo("name", name)
                    .get()
                    .await()

                if (!existing.isEmpty) {
                    // Return failure if organization with the same name already exists
                    return@withContext Result.failure(Exception("Organization with this name already exists"))
                }

                // Generate a new document ID for the organization
                val id = firestore.collection("organizations").document().id

                // Prepare organization data to store in Firestore
                val newOrg = hashMapOf(
                    "id" to id,
                    "name" to name,
                    "code" to code,
                    "owner" to user.uid,
                    "avatarIndex" to 0, // Placeholder index for avatar
                    "members" to emptyList<String>(), // Empty member list
                    "message" to emptyList<Map<String, Any>>(), // Empty message list
                    "lastMessage" to mapOf( // Default structure for last message
                        "content" to "",
                        "senderId" to "",
                        "timestamp" to 0L,
                        "seenBy" to emptyList<String>()
                    )
                )

                // Add the organization document to Firestore
                firestore.collection("organizations")
                    .document(id)
                    .set(newOrg)
                    .await()

                // Return success result
                Result.success("Organization added successfully")
            } catch (e: Exception) {
                // Return failure in case of any error
                Result.failure(Exception("Error adding organization $e"))
            }
        }
    }

    // Function to add a new message to an organization
    override suspend fun addMessage(
        content: String,
        senderId: String,
        currentOrgId: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (currentOrgId.isBlank()) {
                    // Organization ID is required
                    return@withContext Result.failure(Exception("Organization ID is empty"))
                }

                // Create a message map with content, senderId, and timestamp
                val message = mapOf(
                    "content" to content,
                    "senderId" to senderId,
                    "timestamp" to System.currentTimeMillis(),
                )

                // Add the message to the message array field in Firestore
                firestore.collection("organizations")
                    .document(currentOrgId)
                    .update("message", FieldValue.arrayUnion(message))
                    .await()

                // Update the lastMessage field with the new message info
                addUpdateLastMessage(
                    content,
                    senderId,
                    currentOrgId,
                )

                // Return success
                Result.success("Message added successfully")

            } catch (e: Exception) {
                // Return failure on error
                Result.failure(Exception("Failed to add message"))
            }
        }
    }

    // Private helper to update the 'lastMessage' field of an organization
    private suspend fun addUpdateLastMessage(
        content: String,
        senderId: String,
        currentOrgId: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (currentOrgId.isBlank()) {
                    // Return error if ID is blank
                    return@withContext Result.failure(Exception("Organization ID is empty"))
                }

                // Prepare updated last message map
                val lastMessage = mapOf(
                    "content" to content,
                    "senderId" to senderId,
                    "timestamp" to System.currentTimeMillis(),
                    "seenBy" to emptyList<String>()
                )

                // Update lastMessage field in Firestore
                firestore.collection("organizations")
                    .document(currentOrgId)
                    .update("lastMessage", lastMessage)
                    .await()

                Result.success("Last message updated successfully")
            } catch (e: Exception) {
                // Return failure if update fails
                Result.failure(Exception("Failed to update last message"))
            }
        }
    }

    // Function to update the 'seenBy' list for the last message in an organization
    override suspend fun updateSeenByForLastMessage(
        currentOrgId: String,
        currentUserUid: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = firestore.collection("organizations").document(currentOrgId)
                val snapshot = docRef.get().await() // Fetch organization document

                if (!snapshot.exists()) {
                    // Return failure if document doesn't exist
                    return@withContext Result.failure(Exception("Organization not found"))
                }

                // Retrieve owner of the organization
                val owner = snapshot.getString("owner") ?: ""
                if (owner == currentUserUid) {
                    // Owner should not be counted in seenBy
                    return@withContext Result.success("Owner does not count as seen")
                }

                // Get last message map from Firestore
                val lastMessageMap = snapshot.get("lastMessage") as? Map<*, *> ?: run {
                    return@withContext Result.failure(Exception("Last message not found"))
                }

                // Extract current seenBy list
                val seenBy = (lastMessageMap["seenBy"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList()

                if (currentUserUid in seenBy) {
                    // If already seen, skip
                    return@withContext Result.success("User already in seenBy list")
                }

                // Add current user to seenBy list
                val updatedSeenBy = seenBy + currentUserUid
                val updatedLastMessage = lastMessageMap.toMutableMap()
                updatedLastMessage["seenBy"] = updatedSeenBy

                // Update lastMessage field with new seenBy list
                docRef.update("lastMessage", updatedLastMessage).await()

                Result.success("SeenBy list updated successfully")
            } catch (e: Exception) {
                // Return failure on error
                Result.failure(Exception("Failed to update seenBy list: ${e.message}"))
            }
        }
    }


    // This function fetches the organizations owned by the currently authenticated user from Firestore.
    override fun fetchOwnedOrganizations(onUpdate: (List<Organization>) -> Unit) {
        try {
            // Get the currently authenticated user
            val user = firebaseAuth.currentUser ?: run {
                onUpdate(emptyList()) // Return empty list if user is null
                return
            }

            // Query the Firestore collection "organizations" for documents where "owner" field matches the current user's UID
            firestore.collection("organizations")
                .whereEqualTo("owner", user.uid)
                .addSnapshotListener { snapshot, error ->
                    try {
                        // If there’s an error while listening to the snapshot, return empty list
                        if (error != null) {
                            onUpdate(emptyList())
                            return@addSnapshotListener
                        }

                        // If snapshot exists and is not empty
                        if (snapshot != null && !snapshot.isEmpty) {
                            val owned = snapshot.documents.mapNotNull { doc ->
                                try {
                                    // Attempt to retrieve the "lastMessage" field from the document
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

                                    // Attempt to retrieve the "message" list and map it to Message objects
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

                                    // Create and return an Organization object with extracted data
                                    Organization(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        code = doc.getString("code") ?: "",
                                        owner = doc.getString("owner") ?: "",
                                        avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
                                        members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
                                            ?: emptyList(),
                                        messages = messagesList, // Messages list extracted above
                                        lastMessage = lastMessage // Last message extracted above
                                    )
                                } catch (e: Exception) {
                                    null // Ignore the document if parsing fails
                                }
                            }

                            // Pass the final list to the callback
                            onUpdate(owned)
                        } else {
                            onUpdate(emptyList()) // Snapshot was empty
                        }
                    } catch (e: Exception) {
                        onUpdate(emptyList()) // Catch any exception during mapping
                    }
                }

        } catch (e: Exception) {
            onUpdate(emptyList()) // Catch any exception outside Firestore listener
        }
    }



    // This function listens for updates to the "message" field of a specific organization and returns the list of messages.
    override fun fetchMessagesForOrganization(orgId: String, onUpdate: (List<Message>) -> Unit) {
        try {
            // Listen to changes on the specific organization document identified by orgId
            firestore.collection("organizations")
                .document(orgId)
                .addSnapshotListener { snapshot, error ->
                    // If there's an error or document doesn't exist, return empty list
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }

                    try {
                        // Map the "message" field (which is a list of maps) to a list of Message objects
                        val messagesList = (snapshot.get("message") as? List<*>)?.mapNotNull { msg ->
                            (msg as? Map<*, *>)?.let { msgMap ->
                                Message(
                                    content = msgMap["content"] as? String ?: "",
                                    senderId = msgMap["senderId"] as? String ?: "",
                                    timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: 0L
                                )
                            }
                        } ?: emptyList()

                        // Return the message list via callback
                        onUpdate(messagesList)
                    } catch (e: Exception) {
                        onUpdate(emptyList()) // Return empty list on parsing exception
                    }
                }
        } catch (e: Exception) {
            onUpdate(emptyList()) // Return empty list on outer exception
        }
    }



    // This function allows the current logged-in user to join an organization using its name and code.
    override suspend fun joinOrganizationByNameAndCode(name: String, code: String): Result<String> {
        return withContext(Dispatchers.IO) {
            // Get the current user ID from FirebaseAuth. If not logged in, return failure.
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("User not logged in"))

            try {
                // Search for organization that matches the given name and code
                val querySnapshot = firestore.collection("organizations")
                    .whereEqualTo("name", name)
                    .whereEqualTo("code", code)
                    .get()
                    .await()

                // If no organization found, return failure
                if (querySnapshot.isEmpty) {
                    return@withContext Result.failure(Exception("No matching organization found"))
                }

                // Get the first matched organization document
                val document = querySnapshot.documents.first()
                val ownerId = document.getString("owner")
                val members = document.get("members") as? List<String> ?: emptyList()

                // Check if user is already the owner
                if (ownerId == currentUserId) {
                    return@withContext Result.failure(Exception("You are already the owner of this organization"))
                }

                // Check if user is already a member
                if (members.contains(currentUserId)) {
                    return@withContext Result.failure(Exception("You are already a member of this organization"))
                }

                // Add user to members array in Firestore
                val orgRef = document.reference
                orgRef.update("members", FieldValue.arrayUnion(currentUserId)).await()

                // Return success message
                Result.success("Joined organization successfully")
            } catch (e: Exception) {
                // On any error, return failure with generic message
                Result.failure(Exception("Error joining organization"))
            }
        }
    }

    // This function listens in real-time for organizations where the current user is a member and sends updates via the callback
    override fun fetchMemberOrganizations(onUpdate: (List<Organization>) -> Unit) {
        try {
            val currentUid = firebaseAuth.currentUser?.uid ?: run {
                // If user not logged in, return an empty list immediately
                onUpdate(emptyList())
                return
            }

            // Query Firestore for organizations that contain the user in the 'members' array
            firestore.collection("organizations")
                .whereArrayContains("members", currentUid)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null || snapshot == null) {
                            // If error occurred or snapshot is null, return empty list
                            onUpdate(emptyList())
                            return@addSnapshotListener
                        }

                        // Map Firestore documents to Organization objects
                        val memberOf = snapshot.documents.mapNotNull { doc ->
                            try {
                                // Parse 'lastMessage' if available
                                val lastMessageMap = doc.get("lastMessage") as? Map<*, *>
                                val lastMessage = lastMessageMap?.let {
                                    LastMessage(
                                        content = it["content"] as? String ?: "",
                                        senderId = it["senderId"] as? String ?: "",
                                        timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L,
                                        seenBy = (it["seenBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                    )
                                }

                                // Parse list of messages
                                val messagesList = (doc.get("message") as? List<*>)?.mapNotNull { msg ->
                                    (msg as? Map<*, *>)?.let { msgMap ->
                                        Message(
                                            content = msgMap["content"] as? String ?: "",
                                            senderId = msgMap["senderId"] as? String ?: "",
                                            timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: 0L
                                        )
                                    }
                                } ?: emptyList()

                                // Return an Organization object
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
                                // Ignore any document that fails parsing
                                null
                            }
                        }

                        // Send the list to the callback
                        onUpdate(memberOf)
                    } catch (e: Exception) {
                        // If any error occurs during mapping, return empty list
                        onUpdate(emptyList())
                    }
                }

        } catch (e: Exception) {
            // Catch any outer errors and return empty list
            onUpdate(emptyList())
        }
    }

    // Fetch a list of User objects by their IDs from the 'users' collection
    override suspend fun fetchUsersByIds(userIds: List<String>): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                // If list is empty, return empty list
                if (userIds.isEmpty()) return@withContext Result.success(emptyList())

                // Firestore supports only 10 items in 'whereIn', so we chunk the list
                val chunks = userIds.chunked(10)
                val allUsers = mutableListOf<User>()

                // Fetch each chunk separately
                for (chunk in chunks) {
                    val snapshot = firestore.collection("users")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()

                    // Map documents to User objects
                    val users = snapshot.documents.mapNotNull { doc ->
                        val email = doc.getString("email")
                        val fcmToken = doc.getString("fcmToken")
                        val uid = doc.id
                        if (email != null && fcmToken != null) {
                            User(uid, email, fcmToken)
                        } else null // Skip incomplete records
                    }

                    allUsers.addAll(users)
                }

                // Return full list of users
                Result.success(allUsers)
            } catch (e: Exception) {
                // On failure, return the exception
                Result.failure(e)
            }
        }
    }

    // Removes a user from the organization by updating the 'members' array
    override suspend fun removeMemberFromOrganization(
        organizationId: String,
        uidToRemove: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate organization ID
                if (organizationId.isBlank()) {
                    return@withContext Result.failure(Exception("Organization ID is empty"))
                }

                val orgDoc = firestore.collection("organizations").document(organizationId)

                // Remove user from 'members' array
                orgDoc.update("members", FieldValue.arrayRemove(uidToRemove)).await()

                Result.success("Removed successfully")
            } catch (e: Exception) {
                // Catch and wrap any failure
                Result.failure(Exception("Failed to remove"))
            }
        }
    }

    // Updates the avatar index of a given organization
    override suspend fun updateOrganizationAvatarIndex(
        orgId: String,
        newAvatarIndex: Int,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure user is logged in
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                // Update the avatarIndex field
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