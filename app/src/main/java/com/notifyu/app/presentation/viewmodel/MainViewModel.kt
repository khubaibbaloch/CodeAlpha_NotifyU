package com.notifyu.app.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.data.model.User
import com.notifyu.app.domain.usecase.auth.AuthUseCases
import com.notifyu.app.domain.usecase.notification.NotificationUseCase
import com.notifyu.app.domain.usecase.organization.OrganizationUseCase
import com.notifyu.app.domain.usecase.user.UserUseCase
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.viewmodel.states.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.String
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val organizationUseCase: OrganizationUseCase,
    private val userUseCase: UserUseCase,
    private val notificationUseCase: NotificationUseCase,
) : ViewModel() {


    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

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

    private val _selectedOrganization = MutableStateFlow<Organization?>(null)
    val selectedOrganization: StateFlow<Organization?> = _selectedOrganization

//    private val _orgUsers = MutableStateFlow<List<User>>(emptyList())
//    val orgUsers: StateFlow<List<User>> = _orgUsers

    private val _orgEmails = MutableStateFlow<List<String>>(emptyList())
    val orgEmails: StateFlow<List<String>> = _orgEmails

    private val _orgFcmTokens = MutableStateFlow<List<String>>(emptyList())
    val orgFcmTokens: StateFlow<List<String>> = _orgFcmTokens

    private val _orgUids = MutableStateFlow<List<String>>(emptyList())
    val orgUids: StateFlow<List<String>> = _orgUids

//    private val _isOwner = MutableStateFlow<Boolean>(false)
//    val isOwner: StateFlow<Boolean> = _isOwner

//    private val _shouldRedirectHome = MutableStateFlow(false)
//    val shouldRedirectHome: StateFlow<Boolean> = _shouldRedirectHome

    // UI STATE USING

    private val _emailValidationError = MutableStateFlow<Boolean>(false)
    val emailValidationError: StateFlow<Boolean> = _emailValidationError

    private val _passwordValidationError = MutableStateFlow<Boolean>(false)
    val passwordValidationError: StateFlow<Boolean> = _passwordValidationError

    private val _confirmPasswordValidationError = MutableStateFlow<Boolean>(false)
    val confirmPasswordValidationError: StateFlow<Boolean> = _confirmPasswordValidationError


    private val _signingState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val signingState: StateFlow<UiState<String>> = _signingState

    private val _emailVerificationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val emailVerificationState: StateFlow<UiState<String>> = _emailVerificationState

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginState: StateFlow<UiState<String>> = _loginState

    private val _resetPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState<String>> = _resetPasswordState

    private val _createJoinOrgState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val createJoinOrgState: StateFlow<UiState<String>> = _createJoinOrgState


    private val _navigation = MutableStateFlow<AuthNavEvent>(AuthNavEvent.None)
    val navigation: StateFlow<AuthNavEvent> = _navigation

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

//    private val _uiMessage = MutableStateFlow<String>("")
//    val uiMessage: StateFlow<String> = _uiMessage

//    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
//    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        observeAuthState()
    }

//    init {
//        auth.addAuthStateListener{ firebaseAuth ->
//            _currentUser.value = firebaseAuth.currentUser
//            firebaseAuth.currentUser?.let {
//                authFetchOwnedOrganizations()
//                authFetchMemberOrganizations()
//                authSyncFcmTokenIfChanged()
//                checkUserAndNavigate(currentUser = firebaseAuth.currentUser)
//            }
//        }
//    }


    // Commen Functions
    private fun observeAuthState() {
        viewModelScope.launch {
            val authState = authUseCases.authStateUseCase()
            authState.collect { firebaseUser ->
                firebaseUser?.let {
                    _currentUser.value = firebaseUser
                    authFetchOwnedOrganizations()
                    authFetchMemberOrganizations()
                    authSyncFcmTokenIfChanged()
                    checkUserAndNavigate(it)
                    authFetchOwnedOrganizations()
                    authFetchMemberOrganizations()
                    for (i in 1..100) {
                        Log.d("Test", "observeAuthState: $i")
                    }

                }

            }
        }
    }

    fun determineStartDestination() {
        viewModelScope.launch {
            val user = auth.currentUser
            _startDestination.value = if (user != null && user.isEmailVerified) {
                MainScreenRoutes.MainScreenRoot.route
            } else {
                AuthScreenRoutes.AuthScreenRoot.route
            }
        }
    }

    fun validateEmail(email: String): Boolean {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        _emailValidationError.value = !isValid
        return isValid
    }

    fun validatePassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        val isValid = passwordPattern.matches(password)
        _passwordValidationError.value = !isValid
        return isValid
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        val isValid = password == confirmPassword
        _confirmPasswordValidationError.value = !isValid
        return isValid
    }

    fun authSyncFcmTokenIfChanged() {
        viewModelScope.launch {
            notificationUseCase.syncFcmToken()
        }
    }

    fun updateAddOrg(value: Boolean) {
        _isAddOrg.value = value
        _createJoinOrgState.value = UiState.Idle
    }

    fun updateOnOrganizationClick(orgId: String) {
        _onOrganizationClick.value = orgId
    }

    fun signOut() {
        viewModelScope.launch {
            authUseCases.signOutUseCase()
        }
    }


    // Signup Screen Functions
    fun onSignupClicked(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _signingState.value = UiState.Error("Please fill in all fields")
            return
        }
        if (!validateEmail(email) || !validatePassword(password) || !validateConfirmPassword(
                password,
                confirmPassword
            )
        ) {
            _signingState.value = UiState.Error(" Validation failed")
            return
        }
        signup(email, password)
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _signingState.value = UiState.Loading
            val result = authUseCases.signUp(email = email, password = password)
            result.onSuccess { message ->
                userUseCase.createUser(message)
                _signingState.value = UiState.Success("Account created")
                _navigation.value = AuthNavEvent.ToVerifyEmail
                //onResult(true, "Account created")
            }.onFailure { error ->
                _signingState.value =
                    UiState.Error(error.message ?: "An error occurred. Please try again.")
                // onResult(false, "An error occurred. Please try again.")
            }
        }
    }


    fun checkUserAndNavigate(currentUser: FirebaseUser?) {
        val user = currentUser
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
        _signingState.value = UiState.Idle
        _emailVerificationState.value = UiState.Idle
        _loginState.value = UiState.Idle
        _resetPasswordState.value = UiState.Idle
    }

//    fun clearUiMessage() {
//        _uiMessage.value = ""
//    }

    // Verify Email Screen Functions
    fun onVerifyEmailClicked(email: String, currentUser: FirebaseUser?) {
        if (email.isBlank()) {
            _emailVerificationState.value = UiState.Error("Please fill the field")
            return
        }
        if (!validateEmail(email)) {
            _emailVerificationState.value = UiState.Error("Validation failed")
            return
        }
        if (currentUser == null) {
            _emailVerificationState.value = UiState.Error("No user found")
            return
        }
        if (currentUser.email != email) {
            _emailVerificationState.value = UiState.Error("Wrong email")
            return
        }
        authSendEmailVerification()
    }


    fun authSendEmailVerification() {
        viewModelScope.launch {
            _emailVerificationState.value = UiState.Loading
            val result = authUseCases.sendEmailVerification()
            result.onSuccess { message ->
                //_emailVerificationState.value = UiState.Success(message)
                //  onResult(true, message)
            }.onFailure { error ->
                _emailVerificationState.value = UiState.Error(error.message ?: "Unknown error")
                // onResult(true, error.message ?: "Unknown error")
            }
        }
    }


    fun authCheckEmailVerification() {
        viewModelScope.launch {
            val result = authUseCases.checkEmailVerification()
            result.onSuccess { message ->
                // onResult(message)
                if (message) {
                    _emailVerificationState.value = UiState.Success("Email verified")
                    _navigation.value = AuthNavEvent.ToHome
                }
            }.onFailure { error ->
                //onResult(false)
            }
        }
    }


    // Login Screen Functions
    fun onLoginClicked(email: String, password: String, currentUser: FirebaseUser?) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = UiState.Error("Please fill in all fields")
            //_uiMessage.value = "Please fill in all fields"
            return
        }
        if (!validateEmail(email)) {
            _loginState.value = UiState.Error("Validation failed")
            return
        }
        authLoginWithEmail(email, password, onResult = { isSuccess ->
            val user = currentUser
            if (isSuccess) {
                if (user!!.isEmailVerified) {
                    _navigation.value = AuthNavEvent.ToHome
                } else {
                    _navigation.value = AuthNavEvent.ToVerifyEmail
                }
            }
        })
    }


    fun authLoginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = authUseCases.login(email = email, password = password)
            result.onSuccess { message ->
                onResult(true)
                _loginState.value = UiState.Success(message)
            }.onFailure { error ->
                _loginState.value = UiState.Error(error.message ?: "Unknown error")
                onResult(false)
            }
        };
    }


    // Update Password Screen Functions
    fun onResetPasswordClicked(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = UiState.Error("Please fill the field")
            return
        }
        if (!validateEmail(email)
        ) {
            _resetPasswordState.value = UiState.Error(" Validation failed")
            return
        }
        authSendPasswordResetEmail(email)
    }

    fun authSendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading
            val result = authUseCases.sendPasswordReset(email)
            result.onSuccess {
                _resetPasswordState.value =
                    UiState.Success("Password reset email sent successfully")
            }.onFailure { error ->
                _resetPasswordState.value =
                    UiState.Error(error.message ?: "Failed to send reset email")
            }
        }
    }

// Not in use REPLACED by the authSendPasswordResetEmail()
//    fun authUpdatePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
//        viewModelScope.launch {
//
//            val result = authUseCases.updatePassword(newPassword = newPassword)
//            result.onSuccess {
//                onResult(true, "Password updated successfully")
//            }.onFailure {
//                onResult(false, "Password update failed")
//            }
//        }
//    }


// Main Screen and Home Screen Functions

    fun authFetchOwnedOrganizations() {
        organizationUseCase.getOwnedOrgs {
            _organizationsOwned.value = it
        }
    }

    fun authFetchMemberOrganizations() {
        organizationUseCase.getMemberOrgs {
            _organizationsMemberOf.value = it
        }
    }


    // Create or Join Screen Functions
    fun authAddOrganization(name: String, code: String) {
        viewModelScope.launch {
            _createJoinOrgState.value = UiState.Loading
            val result = organizationUseCase.addOrg(name = name, code = code)
            result.onSuccess { message ->
                Log.d("orgDebug", "org : ${message}")
                _createJoinOrgState.value = UiState.Success(message)
            }.onFailure { error ->
                Log.d("orgDebug", "org : ${error.localizedMessage}")
                _createJoinOrgState.value = UiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun authJoinOrganizationByNameAndCode(
        name: String,
        code: String,
    ) {
        viewModelScope.launch {
            _createJoinOrgState.value = UiState.Loading
            val result = organizationUseCase.joinOrg(name = name, code = code)
            result.onSuccess { message ->
                _createJoinOrgState.value = UiState.Success(message)
            }.onFailure { error ->
                _createJoinOrgState.value = UiState.Error(error.message ?: "Unknown error")

            }
        }
    }


    // Chat Screen Functions
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

    // Not In Use
    fun authFetchMessagesForOrganization(orgId: String) {
        organizationUseCase.getOrgMessages(orgId = orgId, onUpdate = {
            _onOrgMessages.value = it
        })
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

    fun authSendFcmPushNotification(
        context: Context,
        targetTokens: List<String>,
        title: String,
        body: String,
        orgId: String,
        orgName : String
    ) {
        viewModelScope.launch {

            notificationUseCase.sendPushNotification(
                context = context,
                targetTokens = targetTokens,
                title = title,
                body = body,
                orgId = orgId,
                orgName = orgName
            )
        }
    }

    fun authUpdateOrganizationAvatarIndex(
        orgId: String,
        newAvatarIndex: Int,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val result = organizationUseCase.updateAvatarIndex(
                orgId = orgId,
                newAvatarIndex = newAvatarIndex
            )
            result.onSuccess { message ->
                onResult(true, message)
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error")
            }
        }
    }


    fun updateSelectedOrganization(
        organizationOwned: List<Organization>,
        organizationsMemberOf: List<Organization>,
        organizationId: String,
    ) {
        val selected = organizationOwned.find { it.id == organizationId }
            ?: organizationsMemberOf.find { it.id == organizationId }

        _selectedOrganization.value = selected
    }


    fun fetchAndCheckOrgUsers(
        memberIds: List<String>,
        currentUid: String?,
        isOwner: Boolean,
    ) {

        _orgEmails.value = emptyList()
        _orgFcmTokens.value = emptyList()
        _orgUids.value = emptyList()

//        if (memberIds.isEmpty()) {
//            _orgUsers.value = emptyList()
//            return
//        }

        authFetchUsersByIds(memberIds) { fetchedUsers ->
            //_orgUsers.value = fetchedUsers

            _orgEmails.value = fetchedUsers.map { it.email }
            _orgFcmTokens.value = fetchedUsers.map { it.fcmToken }
            _orgUids.value = fetchedUsers.map { it.uid }

            val uids = fetchedUsers.map { it.uid }

            Log.d(
                "navBack",
                "fetchAndCheckOrgUsers: Owner ${isOwner} and ${currentUid !in uids} "
            )
            if (currentUid != null && !isOwner && currentUid !in uids) {
                Log.d(
                    "navBack",
                    "fetchAndCheckOrgUsers: Owner ${isOwner} and ${currentUid !in uids} "
                )
                _navigation.value = AuthNavEvent.ToHome
            }
        }
    }

    fun updateSeenByForLastMessage(
        currentOrgId: String,
        currentUserUid: String,
    ) {
        viewModelScope.launch {
            val result = organizationUseCase.updateSeenByForLastMessage(
                currentOrgId = currentOrgId,
                currentUserUid = currentUserUid
            )
        }
    }


//    fun updateIsOwner(currentUserUid: String, selectedOrg: Organization?) {
//        _isOwner.value = selectedOrg?.owner == currentUserUid
//    }


}