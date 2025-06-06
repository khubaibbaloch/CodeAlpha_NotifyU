package com.notifyu.app.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
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
import com.notifyu.app.domain.usecase.auth.AuthUseCases
import com.notifyu.app.domain.usecase.auth.CheckEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.LoginWithEmailUserCase
import com.notifyu.app.domain.usecase.auth.SendEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.notifyu.app.domain.usecase.auth.SignUpUseCase
import com.notifyu.app.domain.usecase.auth.UpdatePasswordUseCase
import com.notifyu.app.domain.usecase.notification.NotificationUseCase
import com.notifyu.app.domain.usecase.organization.AddOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.OrganizationUseCase
import com.notifyu.app.domain.usecase.user.UserUseCase
import com.notifyu.app.presentation.viewmodel.states.UiState
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
import androidx.compose.runtime.*
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val organizationUseCase: OrganizationUseCase,
    private val userUseCase: UserUseCase,
    private val notificationUseCase: NotificationUseCase
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

    // UI STATE USING

    private val _emailValidationError = MutableStateFlow<Boolean>(false)
    val emailValidationError: StateFlow<Boolean> = _emailValidationError

    private val _passwordValidationError = MutableStateFlow<Boolean>(false)
        val passwordValidationError : StateFlow<Boolean> = _passwordValidationError

    private val _confirmPasswordValidationError = MutableStateFlow<Boolean>(false)
    val confirmPasswordValidationError : StateFlow<Boolean> = _confirmPasswordValidationError


    private val _isSigning = MutableStateFlow<UiState<String>>(UiState.Idle)
    val isSigning : StateFlow<UiState<String>> = _isSigning

    private val _navigation = MutableStateFlow<AuthNavEvent>(AuthNavEvent.None)
    val navigation: StateFlow<AuthNavEvent> = _navigation

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()



    init {
        auth.addAuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let {
                authFetchOwnedOrganizations()
                authFetchMemberOrganizations()
                authSyncFcmTokenIfChanged()
                checkUserAndNavigate()
            }
        }
    }

    // SIGNUP FUNTICONS
    fun validateEmail(email: String): Boolean {
        val valid = authUseCases.signUp.validateEmail(email)
        _emailValidationError.value = !valid
        return valid
    }

    fun validatePassword(password: String): Boolean {
        val valid = authUseCases.signUp.validatePassword(password)
        _passwordValidationError.value = !valid
        return valid
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        val valid = authUseCases.signUp.validateConfirmPassword(password,confirmPassword)
        _confirmPasswordValidationError.value = !valid
        return valid
    }

    fun Signup(email: String, password: String) {
        viewModelScope.launch {
            _isSigning.value = UiState.Loading
            val result = authUseCases.signUp.signUp(email = email, password = password)
            result.onSuccess { message ->
                userUseCase.createUser(message)
                _isSigning.value = UiState.Success("Account created")
                _navigation.value = AuthNavEvent.ToVerifyEmail
                //onResult(true, "Account created")
            }.onFailure { error ->
                _isSigning.value = UiState.Error("An error occurred. Please try again.")
               // onResult(false, "An error occurred. Please try again.")
            }
        }
    }

    fun onSignupClicked(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            viewModelScope.launch {
                _isSigning.value = UiState.Error("Please fill in all fields")
                }
            return
        }
        if (!validateEmail(email) || !validatePassword(password) || !validateConfirmPassword(password, confirmPassword)) {
            viewModelScope.launch {
                _isSigning.value = UiState.Error("Validation failed") }
            return
        }
        Signup(email,password)
    }

    fun checkUserAndNavigate() {
        val user = auth.currentUser
        _navigation.value = when {
            user != null && user.isEmailVerified -> AuthNavEvent.ToHome
            user != null && !user.isEmailVerified -> AuthNavEvent.ToVerifyEmail
            else -> AuthNavEvent.None
        }
    }
    fun resetNavigation() {
        _navigation.value = AuthNavEvent.None
        _emailValidationError.value = false
        _passwordValidationError.value = false
        _confirmPasswordValidationError.value = false
    }

    // SIGNUP FUNTICONS
    fun authSendEmailVerification(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authUseCases.sendEmailVerification()
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error")
            }
        }
    }

    fun authCheckEmailVerification(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authUseCases.checkEmailVerification()
            result.onSuccess { message ->
                onResult(message)
            }.onFailure { error ->
                onResult(false)
            }
        }
    }


    fun authLoginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {

            val result = authUseCases.login(email = email, password = password)
            result.onSuccess { message ->
                onResult(true)
            }.onFailure { error ->
                onResult(true)
            }
        }
    }



    // NOT IN USE REPLACED BY THE authSendPasswordResetEmail()
    fun authUpdatePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {

            val result = authUseCases.updatePassword(newPassword = newPassword)
            result.onSuccess {
                onResult(true, "Password updated successfully")
            }.onFailure {
                onResult(false, "Password update failed")
            }
        }
    }


    fun authSendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authUseCases.sendPasswordReset(email)
            result.onSuccess {
                onResult(true, "Email sent to you")
            }.onFailure { error ->
                onResult(false, "Failed to send reset email")
            }
        }
    }

    fun authAddOrganization(name: String, code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {

            val result = organizationUseCase.addOrg(name = name, code = code)
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(false, error.message ?: "Unknown error")
            }
        }
    }


    fun authAddMessage(content: String, senderId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentOrgId = _onOrganizationClick.value
            val result = organizationUseCase.addMsg(
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



    fun authFetchOwnedOrganizations() {
        organizationUseCase.getOwnedOrgs {
            _organizationsOwned.value = it
        }
    }


    fun authFetchMessagesForOrganization(orgId: String) {
        organizationUseCase.getOrgMessages(orgId = orgId, onUpdate = {
            _onOrgMessages.value = it
        })
    }


    fun authJoinOrganizationByNameAndCode(
        name: String,
        code: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val result =  organizationUseCase.joinOrg(name = name, code = code)
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error")

            }
        }
    }


    fun authFetchMemberOrganizations() {

        organizationUseCase.getMemberOrgs {
            _organizationsMemberOf.value = it
        }
    }

    fun authSyncFcmTokenIfChanged() {
        viewModelScope.launch {
            notificationUseCase.syncFcmToken()
        }
    }

    fun authSendFcmPushNotification(
        context: Context,
        targetTokens: List<String>,
        title: String,
        body: String,
    ) {
        viewModelScope.launch {

            notificationUseCase.sendPushNotification(
                context = context,
                targetTokens = targetTokens,
                title = title,
                body = body
            )
        }
    }


    fun authFetchUsersByIds(userIds: List<String>, onResult: (List<User>) -> Unit) {
        viewModelScope.launch {

            val result = organizationUseCase.getUsersByIds(userIds = userIds)
            result.onSuccess { message ->
                onResult(message)
            }.onFailure { error ->
                onResult(emptyList())
            }
        }

    }


    fun authRemoveMemberFromOrganization(
        uidToRemove: String,
        onComplete: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val currentOrgId = _onOrganizationClick.value

            val result = organizationUseCase.removeMember(currentOrgId, uidToRemove)
            result.onSuccess {
                onComplete(true, "removed successfully")
            }.onFailure {
                onComplete(false, "Failed to remove")
            }
        }
    }


    fun authUpdateSelectedScreen(uid: String, selectedScreen: SelectedScreen) {
        viewModelScope.launch {
            val result =
                userUseCase.setSelectedScreen(uid = uid, selectedScreen = selectedScreen)
            result.onSuccess {

            }.onFailure {

            }
        }
    }


    fun authFetchSelectedScreenForCurrentUser(onResult: (String?) -> Unit) {
        userUseCase.getSelectedScreen(onResult = { onResult(it) })
    }


    fun authUpdateOrganizationAvatarIndex(
        orgId: String,
        newAvatarIndex: Int,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {

            val result = organizationUseCase.updateAvatarIndex(orgId = orgId, newAvatarIndex = newAvatarIndex)
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error")
            }
        }
    }


    fun updateAddOrg(value: Boolean) {
        _isAddOrg.value = value
    }

    fun updateOnOrganizationClick(orgId: String) {
        _onOrganizationClick.value = orgId
    }


}