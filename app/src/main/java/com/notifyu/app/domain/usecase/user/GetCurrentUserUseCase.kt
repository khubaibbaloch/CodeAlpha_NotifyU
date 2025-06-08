package com.notifyu.app.domain.usecase.user

import com.google.firebase.auth.FirebaseUser
import com.notifyu.app.domain.repository.UserRepository
import jakarta.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): FirebaseUser? = userRepository.currentUser
}
