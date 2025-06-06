package com.notifyu.app.domain.usecase.auth

import android.util.Patterns
import com.google.firebase.auth.FirebaseUser
import com.notifyu.app.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val authRepository: AuthRepository) {

    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        val pattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return pattern.matches(password)
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return authRepository.signUp(email, password)
    }
}
