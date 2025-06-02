package com.notifyu.app.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.String

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

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
    val onOrgMessages : StateFlow<List<Message>> = _onOrgMessages


    init {
        fetchOwnedOrganizations()
        fetchMemberOrganizations()
    }

    fun signUp(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Sign Up Successful")
                    val user = task.result?.user
                    Log.d("FirebaseAuth", "Sign Up Successful: ${user?.email}")
                } else {
                    onResult(false, "${task.exception?.message}")
                    Log.e("FirebaseAuth", "Sign Up Failed: ${task.exception?.message}")
                }
            }
    }

    fun sendEmailVerification(onResult: (Boolean, String) -> Unit) {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Verification email sent")
                    Log.d(
                        "EmailVerification",
                        "Verification email sent to ${auth.currentUser?.email}"
                    )
                } else {
                    onResult(false, "Failed to send verification email")
                    Log.e("EmailVerification", "Failed to send verification email", task.exception)
                }
            }
    }

    fun checkEmailVerification(onResult: (Boolean) -> Unit) {
        auth.currentUser?.reload()?.addOnCompleteListener {
            if (it.isSuccessful) {
                onResult(auth.currentUser!!.isEmailVerified)
                Log.d(
                    "EmailVerification",
                    "checkEmailVerification ${auth.currentUser!!.isEmailVerified}"
                )
            } else {
                onResult(false)
            }
        }
    }

    fun loginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        Log.d("LoginDebug", "Attempting login with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(
                        "LoginDebug",
                        "Login successful. User: ${user?.email}, Verified: ${user?.isEmailVerified}"
                    )
                    onResult(true)
                } else {
                    Log.e("LoginDebug", "Login failed", task.exception)
                    onResult(false)
                }
            }
    }

    fun updatePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, "Password updated successfully")
                    } else {
                        onResult(false, task.exception?.message ?: "Password update failed")
                    }
                }
        } else {
            onResult(false, "No user is currently signed in")
        }
    }

    fun addOrganization(name: String, code: String, onResult: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User not logged in")
            return
        }

        // Check if an organization with the same name already exists
        db.collection("organizations")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    onResult(false, "Organization with this name already exists")
                } else {
                    // Create organization only if it doesn't already exist
                    val id = db.collection("organizations").document().id
                    val newOrg = hashMapOf(
                        "id" to id,
                        "name" to name,
                        "code" to code,
                        "owner" to currentUser.uid,
                        "members" to emptyList<String>(),
                        "message" to emptyList<Map<String, Any>>()
                    )

                    db.collection("organizations")
                        .document(id)
                        .set(newOrg)
                        .addOnSuccessListener {
                            onResult(true, "Organization added")
                            fetchOwnedOrganizations()
                            Log.d("Firestore", "Organization added with ID: $id")
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Error adding organization")
                            Log.e("Firestore", "Error adding organization", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Error checking organization name")
                Log.e("Firestore", "Error checking organization name", e)
            }
    }


    fun addMessage(content: String, senderId: String, onResult: (Boolean, String) -> Unit) {
        val currentOrgId = _onOrganizationClick.value

        if (currentOrgId.isBlank()) {
            onResult(false, "Organization ID is empty")
            return
        }

        val message = mapOf(
            "content" to content,
            "senderId" to senderId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("organizations")
            .document(currentOrgId)
            .update("message", FieldValue.arrayUnion(message))
            .addOnSuccessListener {
                onResult(true, "Message added to organization array")
                Log.d("Firestore", "Message added to organization: $currentOrgId")
            }
            .addOnFailureListener { e ->
                onResult(false, "Error adding message")
                Log.e("Firestore", "Error updating organization messages array", e)
            }
    }



    fun fetchOwnedOrganizations() {
        val currentUid = auth.currentUser?.uid
        db.collection("organizations")
            .whereEqualTo("owner", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _organizationsOwned.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val owned = snapshot.documents.map { doc ->
                        Organization(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            code = doc.getString("code") ?: "",
                            owner = doc.getString("owner") ?: "",
                            members = (doc.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            messages = emptyList() // leave messages empty for now
                        )
                    }
                    _organizationsOwned.value = owned
                } else {
                    _organizationsOwned.value = emptyList()
                }
            }
    }
    fun fetchMessagesForOrganization(orgId: String) {
        db.collection("organizations")
            .document(orgId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    _onOrgMessages.value = emptyList()
                    return@addSnapshotListener
                }

                val messagesList = (snapshot.get("message") as? List<*>)?.mapNotNull { msg ->
                    (msg as? Map<*, *>)?.let { msgMap ->
                        Message(
                            content = msgMap["content"] as? String ?: "",
                            senderId = msgMap["senderId"] as? String ?: "",
                            timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: 0L
                        )
                    }
                } ?: emptyList()
                _onOrgMessages.value = messagesList
            }
    }

    fun joinOrganizationByNameAndCode(
        name: String,
        code: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onResult(false, "User not logged in")
            return
        }

        db.collection("organizations")
            .whereEqualTo("name", name)
            .whereEqualTo("code", code)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val orgRef = document.reference

                    val ownerId = document.getString("owner")

                    if (ownerId == currentUserId) {
                        onResult(false, "You are already the owner of this organization")
                        return@addOnSuccessListener
                    }

                    // Add user to members array if not already added
                    orgRef.update("members", FieldValue.arrayUnion(currentUserId))
                        .addOnSuccessListener {
                            onResult(true, "Joined organization successfully")
                        }
                        .addOnFailureListener {
                            onResult(false, "Failed to join organization")
                        }
                } else {
                    onResult(false, "No matching organization found")
                }
            }
            .addOnFailureListener {
                onResult(false, "Error fetching organization")
            }
    }




    fun fetchMemberOrganizations() {
        val currentUid = auth.currentUser?.uid ?: run {
            _organizationsMemberOf.value = emptyList()
            return
        }

        db.collection("organizations")
            .whereArrayContains("members", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _organizationsMemberOf.value = emptyList()

                    return@addSnapshotListener
                }

                val memberOf = snapshot.documents.map { doc ->
                    Organization(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        code = doc.getString("code") ?: "",
                        owner = doc.getString("owner") ?: "",
                        members = (doc.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                }
                _organizationsMemberOf.value = memberOf
            }
    }


//
//    fun fetchOrganizations(onResult: (List<Organization>) -> Unit) {
//        val db = Firebase.firestore
//        db.collection("organizations")
//            .get()
//            .addOnSuccessListener { result ->
//                val orgs = result.map { doc ->
//                    Organization(
//                        id = doc.id,
//                        name = doc.getString("name") ?: "",
//                        createdAt = doc.getLong("createdAt") ?: 0L
//                    )
//                }
//                onResult(orgs)
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Error fetching organizations", e)
//                onResult(emptyList())
//            }
//    }
//    fun deleteOrganization(orgId: String) {
//        val db = Firebase.firestore
//        db.collection("organizations").document(orgId)
//            .delete()
//            .addOnSuccessListener {
//                Log.d("Firestore", "Organization deleted")
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Error deleting organization", e)
//            }
//    }


    fun updateAddOrg(value: Boolean) {
        _isAddOrg.value = value
    }

    fun updateOnOrganizationClick(orgId: String) {
        _onOrganizationClick.value = orgId
    }


}