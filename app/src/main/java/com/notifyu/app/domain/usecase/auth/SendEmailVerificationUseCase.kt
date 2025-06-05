package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository

class SendEmailVerificationUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke()= authRepository.sendEmailVerification()
}