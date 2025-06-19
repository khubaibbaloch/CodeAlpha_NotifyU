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
import com.notifyu.app.domain.usecase.auth.data.AuthUseCases
import com.notifyu.app.domain.usecase.notification.data.NotificationUseCase
import com.notifyu.app.domain.usecase.organization.data.OrganizationUseCase
import com.notifyu.app.domain.usecase.user.data.UserUseCase
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.viewmodel.states.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.String
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent

// This ViewModel is annotated with @HiltViewModel to enable dependency injection with Hilt.
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,                 // Use cases related to authentication
    private val organizationUseCase: OrganizationUseCase,  // Use cases for organization logic
    private val userUseCase: UserUseCase,                   // Use cases for user logic
    private val notificationUseCase: NotificationUseCase,   // Use cases for handling notifications
) : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance() // FirebaseAuth instance for auth operations

    // Current Firebase user tracked with StateFlow
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    // Tracks if user is adding an organization
    private val _isAddOrg = MutableStateFlow(false)
    val isAddOrg: StateFlow<Boolean> = _isAddOrg

    // List of organizations the user owns
    private val _organizationsOwned = MutableStateFlow<List<Organization>>(emptyList())
    val organizationsOwned: StateFlow<List<Organization>> = _organizationsOwned

    // List of organizations the user is a member of
    private val _organizationsMemberOf = MutableStateFlow<List<Organization>>(emptyList())
    val organizationsMemberOf: StateFlow<List<Organization>> = _organizationsMemberOf

    // ID of organization clicked by user
    private val _onOrganizationClick = MutableStateFlow<String>("")
    val onOrganizationsClick: StateFlow<String> = _onOrganizationClick

    // Messages associated with the selected organization
    private val _onOrgMessages = MutableStateFlow<List<Message>>(emptyList())
    val onOrgMessages: StateFlow<List<Message>> = _onOrgMessages

    // Organization currently selected
    private val _selectedOrganization = MutableStateFlow<Organization?>(null)
    val selectedOrganization: StateFlow<Organization?> = _selectedOrganization

    // --- Commented-out variables for future use ---
    // private val _orgUsers = MutableStateFlow<List<User>>(emptyList())
    // val orgUsers: StateFlow<List<User>> = _orgUsers

    // Emails of users in the organization
    private val _orgEmails = MutableStateFlow<List<String>>(emptyList())
    val orgEmails: StateFlow<List<String>> = _orgEmails

    // FCM tokens of users in the organization (for push notifications)
    private val _orgFcmTokens = MutableStateFlow<List<String>>(emptyList())
    val orgFcmTokens: StateFlow<List<String>> = _orgFcmTokens

    // UIDs of users in the organization
    private val _orgUids = MutableStateFlow<List<String>>(emptyList())
    val orgUids: StateFlow<List<String>> = _orgUids

    // --- More commented-out variables ---
    // private val _isOwner = MutableStateFlow<Boolean>(false)
    // val isOwner: StateFlow<Boolean> = _isOwner

    // private val _shouldRedirectHome = MutableStateFlow(false)
    // val shouldRedirectHome: StateFlow<Boolean> = _shouldRedirectHome

    // --- UI State Handling ---

    private val _emailValidationError = MutableStateFlow<Boolean>(false)
    val emailValidationError: StateFlow<Boolean> = _emailValidationError

    private val _passwordValidationError = MutableStateFlow<Boolean>(false)
    val passwordValidationError: StateFlow<Boolean> = _passwordValidationError

    private val _confirmPasswordValidationError = MutableStateFlow<Boolean>(false)
    val confirmPasswordValidationError: StateFlow<Boolean> = _confirmPasswordValidationError

    // State representing signing in, loading, error or success
    private val _signingState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val signingState: StateFlow<UiState<String>> = _signingState

    // State for sending email verification
    private val _emailVerificationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val emailVerificationState: StateFlow<UiState<String>> = _emailVerificationState

    // State for login operation
    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginState: StateFlow<UiState<String>> = _loginState

    // State for resetting password
    private val _resetPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState<String>> = _resetPasswordState

    // State for creating or joining an organization
    private val _createJoinOrgState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val createJoinOrgState: StateFlow<UiState<String>> = _createJoinOrgState

    // Navigation event holder
    private val _navigation = MutableStateFlow<AuthNavEvent>(AuthNavEvent.None)
    val navigation: StateFlow<AuthNavEvent> = _navigation

    // Initial screen destination depending on login state
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    // Commented-out message field
    // private val _uiMessage = MutableStateFlow<String>("")
    // val uiMessage: StateFlow<String> = _uiMessage

    // INIT block to begin observing authentication state
    init {
        observeAuthState()
    }

    // --- Old INIT block with auth listener left commented for reference ---
    /*
    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            firebaseAuth.currentUser?.let {
                authFetchOwnedOrganizations()
                authFetchMemberOrganizations()
                authSyncFcmTokenIfChanged()
                checkUserAndNavigate(currentUser = firebaseAuth.currentUser)
            }
        }
    }
    */

    // Function to observe Firebase authentication state changes
    private fun observeAuthState() {
        viewModelScope.launch {
            val authState = authUseCases.authStateUseCase()
            authState.collect { firebaseUser ->
                firebaseUser?.let {
                    _currentUser.value = firebaseUser

                    // Fetch user-owned and member organizations
                    authFetchOwnedOrganizations()
                    authFetchMemberOrganizations()

                    // Sync FCM token if changed
                    authSyncFcmTokenIfChanged()

                    // Route user to correct screen
                    checkUserAndNavigate(it)

                    // These are unnecessarily repeated calls (left unchanged)
                    authFetchOwnedOrganizations()
                    authFetchMemberOrganizations()

                    // Debug log loop to print numbers 1-100
                    for (i in 1..100) {
                        Log.d("Test", "observeAuthState: $i")
                    }
                }
            }
        }
    }

    // Decides which screen to show based on auth state
    fun determineStartDestination() {
        viewModelScope.launch {
            val user = auth.currentUser
            _startDestination.value = if (user != null && user.isEmailVerified) {
                MainScreenRoutes.MainScreenRoot.route // User is logged in and verified
            } else {
                AuthScreenRoutes.AuthScreenRoot.route // Go to login/signup
            }
        }
    }

    // Validates email format using Android's pattern
    fun validateEmail(email: String): Boolean {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        _emailValidationError.value = !isValid
        return isValid
    }

    // Validates password to ensure it has 1 uppercase, 1 digit and at least 8 characters
    fun validatePassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        val isValid = passwordPattern.matches(password)
        _passwordValidationError.value = !isValid
        return isValid
    }

    // Checks if confirmed password matches original password
    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        val isValid = password == confirmPassword
        _confirmPasswordValidationError.value = !isValid
        return isValid
    }

    // Syncs FCM token for push notifications if needed
    fun authSyncFcmTokenIfChanged() {
        viewModelScope.launch {
            notificationUseCase.syncFcmToken()
        }
    }

    // Updates UI state to show/hide add org dialog
    fun updateAddOrg(value: Boolean) {
        _isAddOrg.value = value
        _createJoinOrgState.value = UiState.Idle // Reset state when updating
    }

    // Called when an organization is clicked
    fun updateOnOrganizationClick(orgId: String) {
        _onOrganizationClick.value = orgId
    }


    // Function to sign out the user
    fun signOut() {
        viewModelScope.launch {
            // Calls the signOutUseCase from the auth use cases
            authUseCases.signOutUseCase()
        }
    }


// Signup Screen Functions

    // Called when the sign-up button is clicked
    fun onSignupClicked(email: String, password: String, confirmPassword: String) {
        // Check if any of the fields are empty
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _signingState.value = UiState.Error("Please fill in all fields")
            return
        }

        // Validate the fields: email, password and confirmation
        if (!validateEmail(email) || !validatePassword(password) || !validateConfirmPassword(
                password,
                confirmPassword
            )
        ) {
            _signingState.value = UiState.Error(" Validation failed")
            return
        }

        // If all validations pass, call signup
        signup(email, password)
    }

    // Actual function to perform signup
    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _signingState.value = UiState.Loading  // Set UI state to loading
            val result =
                authUseCases.signUp(email = email, password = password) // Call signup use case
            result.onSuccess { message ->
                userUseCase.createUser(message) // Create user in DB or user repository
                _signingState.value = UiState.Success("Account created")
                _navigation.value =
                    AuthNavEvent.ToVerifyEmail // Navigate to email verification screen
                //onResult(true, "Account created")
            }.onFailure { error ->
                // Show error message if signup fails
                _signingState.value =
                    UiState.Error(error.message ?: "An error occurred. Please try again.")
                // onResult(false, "An error occurred. Please try again.")
            }
        }
    }

    // Checks user state and sets navigation event accordingly
    fun checkUserAndNavigate(currentUser: FirebaseUser?) {
        val user = currentUser
        _navigation.value = when {
            user != null && user.isEmailVerified -> AuthNavEvent.ToHome // Verified user -> go to home
            user != null && !user.isEmailVerified -> AuthNavEvent.ToVerifyEmail // Not verified -> verify email
            else -> AuthNavEvent.None // No user found -> no navigation
        }
    }

    // Resets all auth-related states to idle
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

// Function was commented out, but would clear a UI message
// fun clearUiMessage() {
//     _uiMessage.value = ""
// }


// Verify Email Screen Functions

    // Triggered when verify email button is clicked
    fun onVerifyEmailClicked(email: String, currentUser: FirebaseUser?) {
        // Check if email is empty
        if (email.isBlank()) {
            _emailVerificationState.value = UiState.Error("Please fill the field")
            return
        }

        // Validate email format
        if (!validateEmail(email)) {
            _emailVerificationState.value = UiState.Error("Validation failed")
            return
        }

        // Check if current user exists
        if (currentUser == null) {
            _emailVerificationState.value = UiState.Error("No user found")
            return
        }

        // Check if email matches with the current user's email
        if (currentUser.email != email) {
            _emailVerificationState.value = UiState.Error("Wrong email")
            return
        }

        // If all checks pass, send email verification
        authSendEmailVerification()
    }

    // Sends the email verification
    fun authSendEmailVerification() {
        viewModelScope.launch {
            _emailVerificationState.value = UiState.Loading // Show loading
            val result = authUseCases.sendEmailVerification() // Trigger email verification use case
            result.onSuccess { message ->
                //_emailVerificationState.value = UiState.Success(message)
                //  onResult(true, message)
            }.onFailure { error ->
                _emailVerificationState.value = UiState.Error(error.message ?: "Unknown error")
                // onResult(true, error.message ?: "Unknown error")
            }
        }
    }

    // Checks if user's email has been verified
    fun authCheckEmailVerification() {
        viewModelScope.launch {
            val result = authUseCases.checkEmailVerification() // Call use case
            result.onSuccess { message ->
                // onResult(message)
                if (message) {
                    _emailVerificationState.value = UiState.Success("Email verified")
                    _navigation.value = AuthNavEvent.ToHome // Navigate to home if verified
                }
            }.onFailure { error ->
                //onResult(false)
            }
        }
    }


// Login Screen Functions

    // Called when login button is clicked
    fun onLoginClicked(email: String, password: String, currentUser: FirebaseUser?) {
        // Check for empty fields
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = UiState.Error("Please fill in all fields")
            //_uiMessage.value = "Please fill in all fields"
            return
        }

        // Validate email
        if (!validateEmail(email)) {
            _loginState.value = UiState.Error("Validation failed")
            return
        }

        // Proceed to login if validations pass
        authLoginWithEmail(email, password, onResult = { isSuccess ->
            if (isSuccess) {
                val user = _currentUser.value
                if (user?.isEmailVerified == true) {
                    _navigation.value = AuthNavEvent.ToHome // Verified user -> home
                } else {
                    _navigation.value = AuthNavEvent.ToVerifyEmail // Not verified -> verify email
                }

            }
        })
    }

    // Logs in the user with email and password
    fun authLoginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading // Show loading
            val result = authUseCases.login(email = email, password = password) // Perform login
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

    // Called when reset password button is clicked
    fun onResetPasswordClicked(email: String) {
        // Check for empty email
        if (email.isBlank()) {
            _resetPasswordState.value = UiState.Error("Please fill the field")
            return
        }

        // Validate email
        if (!validateEmail(email)
        ) {
            _resetPasswordState.value = UiState.Error(" Validation failed")
            return
        }

        // Send password reset email
        authSendPasswordResetEmail(email)
    }

    // Sends a password reset email to the given email address.
    fun authSendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading // Show loading state
            val result = authUseCases.sendPasswordReset(email)
            result.onSuccess {
                // On success, update state with success message
                _resetPasswordState.value =
                    UiState.Success("Password reset email sent successfully")
            }.onFailure { error ->
                // On failure, show error message
                _resetPasswordState.value =
                    UiState.Error(error.message ?: "Failed to send reset email")
            }
        }
    }

// Not in use REPLACED by the authSendPasswordResetEmail()
// Updates the current user's password with the given new password
//    fun authUpdatePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
//        viewModelScope.launch {
//            val result = authUseCases.updatePassword(newPassword = newPassword)
//            result.onSuccess {
//                onResult(true, "Password updated successfully")
//            }.onFailure {
//                onResult(false, "Password update failed")
//            }
//        }
//    }

    // Fetches the list of organizations the current user owns
    fun authFetchOwnedOrganizations() {
        organizationUseCase.getOwnedOrgs {
            _organizationsOwned.value = it // Update LiveData with owned orgs
        }
    }

    // Fetches the list of organizations the user is a member of
    fun authFetchMemberOrganizations() {
        organizationUseCase.getMemberOrgs {
            _organizationsMemberOf.value = it // Update LiveData with member orgs
        }
    }

    // Adds a new organization with the given name and code
    fun authAddOrganization(name: String, code: String) {
        viewModelScope.launch {
            _createJoinOrgState.value = UiState.Loading // Show loading state
            val result = organizationUseCase.addOrg(name = name, code = code)
            result.onSuccess { message ->
                Log.d("orgDebug", "org : ${message}")
                _createJoinOrgState.value = UiState.Success(message) // On success, show message
            }.onFailure { error ->
                Log.d("orgDebug", "org : ${error.localizedMessage}")
                _createJoinOrgState.value =
                    UiState.Error(error.message ?: "Unknown error") // Show error
            }
        }
    }

    // Joins an existing organization by name and code
    fun authJoinOrganizationByNameAndCode(
        name: String,
        code: String,
    ) {
        viewModelScope.launch {
            _createJoinOrgState.value = UiState.Loading
            val result = organizationUseCase.joinOrg(name = name, code = code)
            result.onSuccess { message ->
                _createJoinOrgState.value = UiState.Success(message) // Success response
            }.onFailure { error ->
                _createJoinOrgState.value =
                    UiState.Error(error.message ?: "Unknown error") // Failure
            }
        }
    }

    // Adds a message to the currently selected organization chat
    fun authAddMessage(content: String, senderId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentOrgId = _onOrganizationClick.value // Get selected org ID
            val result = organizationUseCase.addMsg(
                content = content,
                senderId = senderId,
                currentOrgId = currentOrgId
            )
            result.onSuccess { message ->
                onResult(true, message) // Message sent successfully
            }.onFailure { error ->
                onResult(false, error.message ?: "Unknown error") // Sending failed
            }
        }
    }

    // Not In Use
// Fetches messages for a given organization (real-time updates)
    fun authFetchMessagesForOrganization(orgId: String) {
        organizationUseCase.getOrgMessages(orgId = orgId, onUpdate = {
            _onOrgMessages.value = it // Update messages
        })
    }

    // Fetches user details by their IDs
    fun authFetchUsersByIds(userIds: List<String>, onResult: (List<User>) -> Unit) {
        viewModelScope.launch {
            val result = organizationUseCase.getUsersByIds(userIds = userIds)
            result.onSuccess { message ->
                onResult(message) // Return list of users
            }.onFailure { error ->
                onResult(emptyList()) // In case of failure, return empty list
            }
        }
    }

    // Removes a member from the currently selected organization
    fun authRemoveMemberFromOrganization(
        uidToRemove: String,
        onComplete: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val currentOrgId = _onOrganizationClick.value
            val result = organizationUseCase.removeMember(currentOrgId, uidToRemove)
            result.onSuccess {
                onComplete(true, "removed successfully") // Member removed
            }.onFailure {
                onComplete(false, "Failed to remove") // Failed to remove
            }
        }
    }

    // Sends an FCM push notification to target tokens
    fun authSendFcmPushNotification(
        context: Context,
        targetTokens: List<String>,
        title: String,
        body: String,
        orgId: String,
        orgName: String,
    ) {
        viewModelScope.launch {
            // Delegates to use case to handle sending
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

    // Updates the avatar index of an organization
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
                onResult(true, message) // Avatar updated
            }.onFailure { error ->
                onResult(true, error.message ?: "Unknown error") // Still returns true?
            }
        }
    }

    // Updates selected organization object from owned or member list by ID
    fun updateSelectedOrganization(
        organizationOwned: List<Organization>,
        organizationsMemberOf: List<Organization>,
        organizationId: String,
    ) {
        val selected = organizationOwned.find { it.id == organizationId }
            ?: organizationsMemberOf.find { it.id == organizationId }

        _selectedOrganization.value = selected // Set selected org
    }

    // Fetches users of an organization and checks if the current user should still be in it
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

            // Map fetched data to respective state flows
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
                _navigation.value = AuthNavEvent.ToHome // Navigate away if removed
            }
        }
    }

    // Updates the 'seen by' status for the last message in the org chat
    fun updateSeenByForLastMessage(
        currentOrgId: String,
        currentUserUid: String,
    ) {
        viewModelScope.launch {
            val result = organizationUseCase.updateSeenByForLastMessage(
                currentOrgId = currentOrgId,
                currentUserUid = currentUserUid
            )
            // No state update is handled here
        }
    }

//    fun updateIsOwner(currentUserUid: String, selectedOrg: Organization?) {
//        _isOwner.value = selectedOrg?.owner == currentUserUid
//    }


}