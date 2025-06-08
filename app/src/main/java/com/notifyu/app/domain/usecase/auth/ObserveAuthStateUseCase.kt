package com.notifyu.app.domain.usecase.auth

import com.google.firebase.auth.FirebaseUser
import com.notifyu.app.domain.repository.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): StateFlow<FirebaseUser?> = repository.observeAuthState()
}
