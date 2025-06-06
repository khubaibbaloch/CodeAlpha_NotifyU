package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository
import javax.inject.Inject

class CheckEmailVerificationUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke () = authRepository.checkEmailVerification()
}