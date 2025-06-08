package com.notifyu.app.domain.usecase.auth

import android.util.Patterns
import com.google.firebase.auth.FirebaseUser
import com.notifyu.app.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke (email: String, password: String) = authRepository.signUp(email,password)
}
