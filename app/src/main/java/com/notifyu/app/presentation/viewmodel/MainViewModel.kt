package com.notifyu.app.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.data.model.SelectedScreen
import com.notifyu.app.data.model.User
import com.notifyu.app.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import kotlin.String
import kotlin.collections.get

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl,
) : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    private val _isAddOrg = MutableStateFlow(false)
    val isAddOrg: StateFlow<Boolean> = _isAddOrg

    private val _organizationsOwned = MutableStateFlow<List<Organization>>(emptyList())
    val organizationsOwned: StateFlow<List<Organization>> = _organizationsOwned

    private val _organizationsMemberOf = MutableStateFlow<List<Organization>>(emptyList())
    val organizationsMemberOf: StateFlow<List<Organization>> = _organizationsMemberOf

    private val _onOrganizationClick = MutableStateFlow<String>("")
    val onOrganizationsClick: StateFlow<String> = _onOrganizationClick

    private val _onOrgMessages = MutableStateFlow<List<Message>>(emptyList())
    val onOrgMessages: StateFlow<List<Message>> = _onOrgMessages


    init {
        // Only listen after Firebase auth is ready
        auth.addAuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let {
                authFetchOwnedOrganizations()
                //fetchOwnedOrganizations()
                authFetchMemberOrganizations()
//                fetchMemberOrganizations()
                authSyncFcmTokenIfChanged()
//                syncFcmTokenIfChanged()
            }
        }
    }

    fun authSignup(email: String, code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signUp(email = email, password = code)
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(false, "An error occurred. Please try again.")
            }
        }
    }


//    fun signUp(email: String, password: String, onResult: (Boolean, String) -> Unit) {
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val user = task.result?.user
//                    if (user != null) {
//                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
//                            if (tokenTask.isSuccessful) {
//                                val token = tokenTask.result
//                                val userData = hashMapOf(
//                                    "uid" to user.uid,
//                                    "email" to user.email,
//                                    "fcmToken" to token,
//                                    "avatarIndex" to 0,
//                                    "selectedScreen" to SelectedScreen.None.value
//
//                                )
//                                Firebase.firestore.collection("users").document(user.uid)
//                                    .set(userData)
//                                    .addOnSuccessListener {
//                                        Log.d("FCM", "User data saved successfully")
//                                    }
//                                    .addOnFailureListener { e ->
//                                        Log.d("FCM", "Failed to save user data", e)
//                                    }
//                            } else {
//                                Log.d("FCM", "Failed to get FCM token", tokenTask.exception)
//                            }
//                        }
//                    }
//                    onResult(true, "Sign Up Successful")
//                    Log.d("FirebaseAuth", "Sign Up Successful: ${user?.email}")
//                } else {
//                    onResult(false, "${task.exception?.message}")
//                    Log.e("FirebaseAuth", "Sign Up Failed: ${task.exception?.message}")
//                }
//            }
//    }

    fun authSendEmailVerification(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendEmailVerification()
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error")
            }
        }
    }

//    fun sendEmailVerification(onResult: (Boolean, String) -> Unit) {
//        auth.currentUser?.sendEmailVerification()
//            ?.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    onResult(true, "Verification email sent")
//                    Log.d(
//                        "EmailVerification",
//                        "Verification email sent to ${auth.currentUser?.email}"
//                    )
//                } else {
//                    onResult(false, "Failed to send verification email")
//                    Log.e("EmailVerification", "Failed to send verification email", task.exception)
//                }
//            }
//    }

    fun authCheckEmailVerification(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.checkEmailVerification()
            result.onSuccess { message ->
                onResult(message)
            }.onFailure { error ->
                onResult(false)
            }
        }
    }
//    fun checkEmailVerification(onResult: (Boolean) -> Unit) {
//        auth.currentUser?.reload()?.addOnCompleteListener {
//            if (it.isSuccessful) {
//                onResult(auth.currentUser!!.isEmailVerified)
//                Log.d(
//                    "EmailVerification",
//                    "checkEmailVerification ${auth.currentUser!!.isEmailVerified}"
//                )
//            } else {
//                onResult(false)
//            }
//        }
//    }

    fun authLoginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email = email, password = password)
            result.onSuccess { message ->
                onResult(true)
            }.onFailure { error ->
                onResult(true)
            }
        }
    }

//    fun loginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
//        Log.d("LoginDebug", "Attempting login with email: $email")
//
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    Log.d(
//                        "LoginDebug",
//                        "Login successful. User: ${user?.email}, Verified: ${user?.isEmailVerified}"
//                    )
//                    onResult(true)
//                } else {
//                    Log.e("LoginDebug", "Login failed", task.exception)
//                    onResult(false)
//                }
//            }
//    }

    // NOT IN USE REPLACED BY THE authSendPasswordResetEmail()
    fun authUpdatePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.updatePassword(newPassword = newPassword)
            result.onSuccess {
                onResult(true, "Password updated successfully")
            }.onFailure {
                onResult(false, "Password update failed")
            }
        }
    }
//
//    fun updatePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
//        val user = auth.currentUser
//        if (user != null) {
//            user.updatePassword(newPassword)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        onResult(true, "Password updated successfully")
//                    } else {
//                        onResult(false, task.exception?.message ?: "Password update failed")
//                    }
//                }
//        } else {
//            onResult(false, "No user is currently signed in")
//        }
//    }

    fun authSendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                onResult(true, "Email sent to you")
            }.onFailure { error ->
                onResult(false, "Failed to send reset email")
            }
        }
    }

    fun authAddOrganization(name: String, code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.addOrganization(name = name, code = code)
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(false, error.message ?: "Unknown error")
            }
        }
    }

//    fun addOrganization(name: String, code: String, onResult: (Boolean, String) -> Unit) {
//        val currentUser = auth.currentUser
//        if (currentUser == null) {
//            onResult(false, "User not logged in")
//            return
//        }
//
//        // Check if an organization with the same name already exists
//        db.collection("organizations")
//            .whereEqualTo("name", name)
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                if (!querySnapshot.isEmpty) {
//                    onResult(false, "Organization with this name already exists")
//                } else {
//                    // Create organization only if it doesn't already exist
//                    val id = db.collection("organizations").document().id
//                    val newOrg = hashMapOf(
//                        "id" to id,
//                        "name" to name,
//                        "code" to code,
//                        "owner" to currentUser.uid,
//                        "avatarIndex" to 0,
//                        "members" to emptyList<String>(),
//                        "message" to emptyList<Map<String, Any>>()
//                    )
//
//                    db.collection("organizations")
//                        .document(id)
//                        .set(newOrg)
//                        .addOnSuccessListener {
//                            onResult(true, "Organization added")
//                            fetchOwnedOrganizations()
//                            Log.d("Firestore", "Organization added with ID: $id")
//                        }
//                        .addOnFailureListener { e ->
//                            onResult(false, "Error adding organization")
//                            Log.e("Firestore", "Error adding organization", e)
//                        }
//                }
//            }
//            .addOnFailureListener { e ->
//                onResult(false, "Error checking organization name")
//                Log.e("Firestore", "Error checking organization name", e)
//            }
//    }


    fun authAddMessage(content: String, senderId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentOrgId = _onOrganizationClick.value
            val result = authRepository.addMessage(
                content = content,
                senderId = senderId,
                currentOrgId = currentOrgId
            )
            result.onSuccess { message ->
                onResult(true, message)
            }
                .onFailure { error ->
                    onResult(false, error.message ?: "Unknown error")
                }
        }
    }


//    fun addMessage(content: String, senderId: String, onResult: (Boolean, String) -> Unit) {
//        val currentOrgId = _onOrganizationClick.value
//
//        if (currentOrgId.isBlank()) {
//            onResult(false, "Organization ID is empty")
//            return
//        }
//
//        val message = mapOf(
//            "content" to content,
//            "senderId" to senderId,
//            "timestamp" to System.currentTimeMillis()
//        )
//
//        db.collection("organizations")
//            .document(currentOrgId)
//            .update("message", FieldValue.arrayUnion(message))
//            .addOnSuccessListener {
//                onResult(true, "Message added to organization array")
//                Log.d("FirestoreSendSmsDebug", "Message added to organization: $currentOrgId")
//            }
//            .addOnFailureListener { e ->
//                onResult(false, "Error adding message")
//                Log.d("FirestoreSendSmsDebug", "Error updating organization messages array", e)
//            }
//    }


    fun authFetchOwnedOrganizations() {
        authRepository.fetchOwnedOrganizations {
            _organizationsOwned.value = it
        }
    }

//    fun fetchOwnedOrganizations() {
//        val currentUid = auth.currentUser?.uid
//        db.collection("organizations")
//            .whereEqualTo("owner", currentUid)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    _organizationsOwned.value = emptyList()
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null && !snapshot.isEmpty) {
//                    val owned = snapshot.documents.map { doc ->
//                        Organization(
//                            id = doc.id,
//                            name = doc.getString("name") ?: "",
//                            code = doc.getString("code") ?: "",
//                            owner = doc.getString("owner") ?: "",
//                            avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
//                            members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
//                                ?: emptyList(),
//                            messages = emptyList() // leave messages empty for now
//                        )
//                    }
//                    _organizationsOwned.value = owned
//                    Log.d("orgOwnedDebug", "Fetched organizationsOwned = ${owned.size}")
//
//                } else {
//                    _organizationsOwned.value = emptyList()
//                }
//            }
//    }


    fun authFetchMessagesForOrganization(orgId: String) {
        authRepository.fetchMessagesForOrganization(orgId = orgId, onUpdate = {
            _onOrgMessages.value = it
        })
    }

//    fun fetchMessagesForOrganization(orgId: String) {
//        db.collection("organizations")
//            .document(orgId)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null || snapshot == null || !snapshot.exists()) {
//                    _onOrgMessages.value = emptyList()
//                    return@addSnapshotListener
//                }
//
//                val messagesList = (snapshot.get("message") as? List<*>)?.mapNotNull { msg ->
//                    (msg as? Map<*, *>)?.let { msgMap ->
//                        Message(
//                            content = msgMap["content"] as? String ?: "",
//                            senderId = msgMap["senderId"] as? String ?: "",
//                            timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: 0L
//                        )
//                    }
//                } ?: emptyList()
//                _onOrgMessages.value = messagesList
//            }
//    }

    fun authJoinOrganizationByNameAndCode(
        name: String,
        code: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val result = authRepository.joinOrganizationByNameAndCode(name = name, code = code)
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error")

            }
        }
    }

//    fun joinOrganizationByNameAndCode(
//        name: String,
//        code: String,
//        onResult: (Boolean, String) -> Unit,
//    ) {
//        val currentUserId = auth.currentUser?.uid ?: run {
//            onResult(false, "User not logged in")
//            return
//        }
//
//        db.collection("organizations")
//            .whereEqualTo("name", name)
//            .whereEqualTo("code", code)
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                if (!querySnapshot.isEmpty) {
//                    val document = querySnapshot.documents.first()
//                    val orgRef = document.reference
//
//                    val ownerId = document.getString("owner")
//
//                    if (ownerId == currentUserId) {
//                        onResult(false, "You are already the owner of this organization")
//                        return@addOnSuccessListener
//                    }
//
//                    // Add user to members array if not already added
//                    orgRef.update("members", FieldValue.arrayUnion(currentUserId))
//                        .addOnSuccessListener {
//                            onResult(true, "Joined organization successfully")
//                        }
//                        .addOnFailureListener {
//                            onResult(false, "Failed to join organization")
//                        }
//                } else {
//                    onResult(false, "No matching organization found")
//                }
//            }
//            .addOnFailureListener {
//                onResult(false, "Error fetching organization")
//            }
//    }


    fun authFetchMemberOrganizations() {
        authRepository.fetchMemberOrganizations {
            _organizationsMemberOf.value = it
        }
    }
//    fun fetchMemberOrganizations() {
//        val currentUid = auth.currentUser?.uid ?: run {
//            _organizationsMemberOf.value = emptyList()
//            return
//        }
//
//        db.collection("organizations")
//            .whereArrayContains("members", currentUid)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null || snapshot == null) {
//                    _organizationsMemberOf.value = emptyList()
//
//                    return@addSnapshotListener
//                }
//
//                val memberOf = snapshot.documents.map { doc ->
//                    Organization(
//                        id = doc.id,
//                        name = doc.getString("name") ?: "",
//                        code = doc.getString("code") ?: "",
//                        owner = doc.getString("owner") ?: "",
//                        avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0,
//                        members = (doc.get("members") as? List<*>)?.filterIsInstance<String>()
//                            ?: emptyList()
//                    )
//                }
//                _organizationsMemberOf.value = memberOf
//            }
//    }


    // FCM

    fun authSyncFcmTokenIfChanged() {
        viewModelScope.launch {
            authRepository.syncFcmTokenIfChanged()
        }
    }
//    fun syncFcmTokenIfChanged() {
//        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
//            val uid = Firebase.auth.currentUser?.uid ?: return@addOnSuccessListener
//            val userDocRef = Firebase.firestore.collection("users").document(uid)
//            userDocRef.get().addOnSuccessListener { doc ->
//                val savedToken = doc.getString("fcmToken")
//                if (savedToken != token) {
//                    userDocRef.update("fcmToken", token)
//                        .addOnSuccessListener {
//                            Log.d("FCM", "Token synced at startup")
//                        }
//                        .addOnFailureListener {
//                            Log.e("FCM", "Failed to sync token at startup", it)
//                        }
//                }
//            }
//        }
//    }


//    fun sendNotificationToUsers(
//        organizationId: String,
//        messageTitle: String,
//        messageBody: String
//    ) {
//        val db = Firebase.firestore
//        val currentUserId = Firebase.auth.currentUser?.uid ?: return
//
//        db.collection("organizations").document(organizationId).get()
//            .addOnSuccessListener { document ->
//                val members = document.get("members") as? List<String> ?: emptyList()
//
//                // Send notification to each member (excluding the sender/admin)
//                for (memberId in members) {
//                    if (memberId == currentUserId) continue
//
//                    db.collection("users").document(memberId).get()
//                        .addOnSuccessListener { userDoc ->
//                            val token = userDoc.getString("fcmToken")
//                            if (!token.isNullOrEmpty()) {
//                                sendFCMToToken(token, messageTitle, messageBody)
//                            }
//                        }
//                }
//            }
//    }
//    fun sendFCMToToken(token: String, title: String, body: String) {
//        val json = JSONObject()
//        val notification = JSONObject()
//        notification.put("title", title)
//        notification.put("body", body)
//        json.put("to", token)
//        json.put("notification", notification)
//
//        val url = URL("https://fcm.googleapis.com/fcm/send")
//        val conn = (url.openConnection() as HttpURLConnection).apply {
//            requestMethod = "POST"
//            setRequestProperty("Authorization", "key=YOUR_SERVER_KEY")
//            setRequestProperty("Content-Type", "application/json")
//            doOutput = true
//        }
//
//        val outputWriter = OutputStreamWriter(conn.outputStream)
//        outputWriter.write(json.toString())
//        outputWriter.flush()
//        outputWriter.close()
//
//        val responseCode = conn.responseCode
//        Log.d("FCM", "Sent FCM to $token: Response $responseCode")
//    }


    fun authSendFcmPushNotification(
        context: Context,
        targetTokens: List<String>,
        title: String,
        body: String,
    ) {
        viewModelScope.launch {
            authRepository.sendFcmPushNotification(
                context = context,
                targetTokens = targetTokens,
                title = title,
                body = body
            )
        }
    }

//    fun sendFcmPushNotification(
//        context: Context,
//        targetTokens: List<String>,
//        title: String,
//        body: String,
//    ) {
//        Thread {
//            try {
//                // 1. Load service account credentials
//                val inputStream: InputStream = context.assets.open("service-account.json")
//                val googleCredentials = GoogleCredentials.fromStream(inputStream)
//                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
//                googleCredentials.refreshIfExpired()
//                val accessToken = googleCredentials.accessToken.tokenValue
//
//                // 2. Loop through each FCM token
//                val client = OkHttpClient()
//                val mediaType = "application/json; charset=utf-8".toMediaType()
//
//                for (token in targetTokens) {
//                    val json = JSONObject()
//                    val message = JSONObject()
//                    val notification = JSONObject()
//
//                    notification.put("title", title)
//                    notification.put("body", body)
//
//                    message.put("token", token)
//                    message.put("notification", notification)
//
//                    json.put("message", message)
//
//                    val requestBody = json.toString().toRequestBody(mediaType)
//
//                    val request = Request.Builder()
//                        .url("https://fcm.googleapis.com/v1/projects/notifyu-82ee4/messages:send")
//                        .addHeader("Authorization", "Bearer $accessToken")
//                        .addHeader("Content-Type", "application/json; UTF-8")
//                        .post(requestBody)
//                        .build()
//
//                    val response = client.newCall(request).execute()
//                    Log.d("FCM", "Token: $token → ${response.code} - ${response.body?.string()}")
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }.start()
//    }

    fun authFetchUsersByIds(userIds: List<String>, onResult: (List<User>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.fetchUsersByIds(userIds = userIds)
            result.onSuccess { message ->
                onResult(message)
            }.onFailure { error ->
                onResult(emptyList())
            }
        }

    }

//    fun fetchUsersByIds(userIds: List<String>, onResult: (List<User>) -> Unit) {
//        if (userIds.isEmpty()) {
//            onResult(emptyList())
//            return
//        }
//        Log.d("orgEmails", "users: ${userIds.size}")
//
//
//        val chunks = userIds.chunked(10)
//        val allUsers = mutableListOf<User>()
//        var remainingChunks = chunks.size
//
//        for (chunk in chunks) {
//            Firebase.firestore.collection("users")
//                .whereIn(FieldPath.documentId(), chunk)
//                .get()
//                .addOnSuccessListener { snapshot ->
//                    val users = snapshot.documents.mapNotNull { doc ->
//                        val email = doc.getString("email")
//                        val fcmToken = doc.getString("fcmToken")
//                        Log.d("orgEmails", "users: ${fcmToken}")
//                        val uid = doc.id
//                        if (email != null && fcmToken != null) {
//                            User(uid, email, fcmToken)
//                        } else null
//                    }
//
//                    allUsers.addAll(users)
//                    remainingChunks--
//
//                    if (remainingChunks == 0) {
//                        onResult(allUsers)
//                    }
//                }
//                .addOnFailureListener {
//                    remainingChunks--
//                    if (remainingChunks == 0) {
//                        onResult(allUsers) // return whatever succeeded
//                    }
//                }
//        }
//    }


    fun authRemoveMemberFromOrganization(
        uidToRemove: String,
        onComplete: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val currentOrgId = _onOrganizationClick.value

            val result = authRepository.removeMemberFromOrganization(currentOrgId, uidToRemove)
            result.onSuccess {
                onComplete(true, "removed successfully")
            }.onFailure {
                onComplete(false, "Failed to remove")
            }
        }
    }
//    fun removeMemberFromOrganization(uidToRemove: String, onComplete: (Boolean, String) -> Unit) {
//        val currentOrgId = _onOrganizationClick.value // your current organization ID
//
//        if (currentOrgId.isBlank()) {
//            onComplete(false, "Organization ID is empty")
//            return
//        }
//
//        val orgDoc = db.collection("organizations").document(currentOrgId)
//
//        orgDoc.update("members", FieldValue.arrayRemove(uidToRemove))
//            .addOnSuccessListener {
//                Log.d("RemoveMember", "Successfully removed $uidToRemove from $currentOrgId")
//                onComplete(true, "removed successfully")
//            }
//            .addOnFailureListener { e ->
//                Log.e("RemoveMember", "Failed to remove member", e)
//                onComplete(false, "Failed to remove")
//            }
//    }

    fun authUpdateSelectedScreen(uid: String, selectedScreen: SelectedScreen) {
        viewModelScope.launch {
            val result =
                authRepository.updateSelectedScreen(uid = uid, selectedScreen = selectedScreen)
            result.onSuccess {

            }.onFailure {

            }
        }
    }

//    fun updateSelectedScreen(uid: String, selectedScreen: SelectedScreen) {
//        val screenValue = selectedScreen.value
//
//        Firebase.firestore.collection("users").document(uid)
//            .update("selectedScreen", screenValue)
//            .addOnSuccessListener {
//                Log.d("Firestore", "selectedScreen updated to $screenValue")
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Failed to update selectedScreen", e)
//            }
//    }

    fun authFetchSelectedScreenForCurrentUser(onResult: (String?) -> Unit) {
        authRepository.fetchSelectedScreenForCurrentUser(onResult = { onResult(it) })
    }

//    fun fetchSelectedScreenForCurrentUser(onResult: (String?) -> Unit) {
//        val currentUser = auth.currentUser
//        if (currentUser == null) {
//            onResult(null)
//            return
//        }
//
//        Firebase.firestore.collection("users").document(currentUser.uid)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null || snapshot == null || !snapshot.exists()) {
//                    onResult(null)
//                    return@addSnapshotListener
//                }
//
//                val selectedScreen = snapshot.getString("selectedScreen")
//                onResult(selectedScreen)
//            }
//    }

    fun authUpdateOrganizationAvatarIndex(
        orgId: String,
        newAvatarIndex: Int,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val result = authRepository.updateOrganizationAvatarIndex(orgId = orgId, newAvatarIndex = newAvatarIndex)
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error")
            }
        }
    }

//    fun updateOrganizationAvatarIndex(
//        orgId: String,
//        newAvatarIndex: Int,
//        onResult: (Boolean, String) -> Unit,
//    ) {
//        val currentUser = auth.currentUser
//        if (currentUser == null) {
//            onResult(false, "User not logged in")
//            return
//        }
//
//        // Update the avatarIndex field of the organization document
//        db.collection("organizations")
//            .document(orgId)
//            .update("avatarIndex", newAvatarIndex)
//            .addOnSuccessListener {
//                onResult(true, "Avatar updated successfully")
//                Log.d("Firestore", "Avatar index updated for organization: $orgId")
//            }
//            .addOnFailureListener { e ->
//                onResult(false, "Failed to update avatar")
//                Log.e("Firestore", "Error updating avatar index", e)
//            }
//    }


    fun updateAddOrg(value: Boolean) {
        _isAddOrg.value = value
    }

    fun updateOnOrganizationClick(orgId: String) {
        _onOrganizationClick.value = orgId
    }


}