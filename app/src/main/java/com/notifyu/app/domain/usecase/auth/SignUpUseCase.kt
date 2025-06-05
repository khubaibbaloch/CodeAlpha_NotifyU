package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository

class SignUpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = authRepository.signUp(email, password)
}