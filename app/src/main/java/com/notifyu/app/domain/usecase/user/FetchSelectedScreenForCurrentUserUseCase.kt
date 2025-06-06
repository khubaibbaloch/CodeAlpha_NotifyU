package com.notifyu.app.domain.usecase.user

import com.notifyu.app.domain.repository.OrganizationRepository
import com.notifyu.app.domain.repository.UserRepository
import javax.inject.Inject

class FetchSelectedScreenForCurrentUserUseCase @Inject constructor(private val userRepository: UserRepository)  {
    operator fun invoke(onResult: (String?) -> Unit) =
        userRepository.fetchSelectedScreenForCurrentUser(onResult = onResult)
}