package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository

class UpdatePasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke (newPassword: String) = authRepository.updatePassword(newPassword)
}