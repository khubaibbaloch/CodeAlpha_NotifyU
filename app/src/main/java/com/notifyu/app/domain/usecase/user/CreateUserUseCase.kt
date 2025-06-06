package com.notifyu.app.domain.usecase.user

import com.google.firebase.auth.FirebaseUser
import com.notifyu.app.domain.repository.UserRepository
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(private val userRepository: UserRepository)  {
    suspend operator fun invoke(user: FirebaseUser) =
        userRepository.createUser(user = user)
}