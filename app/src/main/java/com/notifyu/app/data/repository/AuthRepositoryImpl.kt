package com.notifyu.app.data.repository


import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.notifyu.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


// Implementation of AuthRepository using FirebaseAuth for authentication
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Injecting FirebaseAuth instance
) : AuthRepository {

    // MutableStateFlow to hold the current FirebaseUser
    private val _authStateFlow = MutableStateFlow(firebaseAuth.currentUser)

    // Publicly exposed immutable StateFlow to observe authentication state changes
    val authStateFlow: StateFlow<FirebaseUser?> = _authStateFlow

    init {
        // Adding an auth state listener that updates the flow when auth state changes
        firebaseAuth.addAuthStateListener {
            _authStateFlow.value = it.currentUser
        }
    }

    // Exposes a method to observe authentication state externally
    override fun observeAuthState(): StateFlow<FirebaseUser?> = authStateFlow

    // Registers a new user with the given email and password
    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                // Create user with email and password using Firebase
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                // Return user if not null, else return failure
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User is null"))
                }
            } catch (e: Exception) {
                // Catch and return any exception occurred
                Result.failure(Exception(e.localizedMessage ?: "Unknown Error"))
            }
        }

    // Sends a verification email to the currently logged-in user
    override suspend fun sendEmailVerification(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val user = Firebase.auth.currentUser // Getting the current user from Firebase
                if (user == null) {
                    // Return failure if user is not logged in
                    return@withContext Result.failure(Exception("User not logged in"))
                }
                user.sendEmailVerification().await() // Send email verification
                Result.success("Verification email sent successfully")
            } catch (e: Exception) {
                // Return failure in case of error
                Result.failure(Exception(e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    // Checks if the user's email is verified
    override suspend fun checkEmailVerification(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = Firebase.auth.currentUser // Get current user
                if (user == null) {
                    // Log message is unreachable due to return before it
                    return@withContext Result.failure(Exception("User not logged in"))
                    Log.d("checkEmailVerification", "checkEmailVerification:false 1") // This line will never execute
                }
                user.reload().await() // Refresh the user's data
                Log.d("checkEmailVerification", "checkEmailVerification:${user.isEmailVerified} ")
                Result.success(user.isEmailVerified) // Return whether email is verified
            } catch (e: Exception) {
                // Catch block with log and error handling
                Log.d("checkEmailVerification", "checkEmailVerification: false 2")
                Result.failure(Exception(e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    // Logs in a user using email and password
    override suspend fun loginWithEmail(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await() // Attempt login
                Result.success("Login successful") // Return success
            } catch (e: Exception) {
                // Return failure on exception
                Result.failure(Exception(e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    // Updates the password for the currently logged-in user
    override suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                if (user == null) {
                    // Return failure if user not logged in
                    return@withContext Result.failure(Exception("User not logged in"))
                }
                user.updatePassword(newPassword).await() // Update password
                Result.success(true) // Return success
            } catch (e: Exception) {
                // Return failure on exception
                Result.failure(Exception(e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    // Sends a password reset email to the specified email
    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await() // Send reset email
                Result.success(true)
            } catch (e: Exception) {
                // Return failure on exception
                Result.failure(Exception(e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    // Signs out the current user
    override suspend fun signOut(): Result<String> {
        return withContext(Dispatchers.Default) {
            try {
                firebaseAuth.signOut() // Perform sign out
                Result.success("Sign out successful")
            } catch (e: Exception) {
                // Return failure on exception
                Result.failure(Exception("Sign out failed: ${e.message}"))
            }
        }
    }
}



