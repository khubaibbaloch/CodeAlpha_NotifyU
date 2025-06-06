package com.notifyu.app.domain.repository

import com.google.firebase.auth.FirebaseUser
import javax.inject.Singleton


interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun loginWithEmail(email: String, password: String): Result<String>
    suspend fun sendEmailVerification(): Result<String>
    suspend fun checkEmailVerification(): Result<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean>
    suspend fun updatePassword(newPassword: String): Result<Boolean>
}
