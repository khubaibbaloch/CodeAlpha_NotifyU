package com.notifyu.app.domain.usecase.auth

import com.notifyu.app.domain.repository.AuthRepository
import com.notifyu.app.domain.repository.UserRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke ()= authRepository.signOut()
}