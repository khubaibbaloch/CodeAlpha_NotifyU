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


class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
): AuthRepository {

    private val _authStateFlow = MutableStateFlow(firebaseAuth.currentUser)
    val authStateFlow: StateFlow<FirebaseUser?> = _authStateFlow

    init {
        firebaseAuth.addAuthStateListener {
            _authStateFlow.value = it.currentUser
        }
    }

    override fun observeAuthState(): StateFlow<FirebaseUser?> = authStateFlow


    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User is null"))
                }
            } catch (e: Exception) {
                Result.failure(Exception( e.localizedMessage ?: "Unknown Error"))
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
                Result.failure(Exception( e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    override suspend fun checkEmailVerification(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = Firebase.auth.currentUser
                if (user == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                    Log.d("checkEmailVerification", "checkEmailVerification:false 1")
                }
                user.reload().await()
                Log.d("checkEmailVerification", "checkEmailVerification:${user.isEmailVerified} ")
                Result.success(user.isEmailVerified)
            } catch (e: Exception) {
                Log.d("checkEmailVerification", "checkEmailVerification: false 2")
                Result.failure(Exception( e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                Result.success("Login successful")
            } catch (e: Exception) {
                Result.failure(Exception( e.localizedMessage ?: "Unknown Error"))
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
                Result.failure(Exception( e.localizedMessage ?: "Unknown Error"))
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(Exception( e.localizedMessage ?: "Unknown Error"))
            }
        }
    }
    override suspend fun signOut(): Result<String> {
        return withContext(Dispatchers.Default) {
            try {
                firebaseAuth.signOut()
                Result.success("Sign out successful")
            } catch (e: Exception) {
                Result.failure(Exception("Sign out failed: ${e.message}"))
            }
        }
    }
}


