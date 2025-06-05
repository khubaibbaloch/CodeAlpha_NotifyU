package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository

class LoginWithEmailUserCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        authRepository.loginWithEmail(email = email, password = password)
}