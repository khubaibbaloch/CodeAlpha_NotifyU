package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository

class SendPasswordResetEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke (email: String) = authRepository.sendPasswordResetEmail(email)
}