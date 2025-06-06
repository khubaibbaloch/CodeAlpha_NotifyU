package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke (newPassword: String) = authRepository.updatePassword(newPassword)
}