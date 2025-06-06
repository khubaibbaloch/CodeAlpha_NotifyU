package com.notifyu.app.domain.usecase.user

import com.notifyu.app.data.model.SelectedScreen
import com.notifyu.app.domain.repository.OrganizationRepository
import com.notifyu.app.domain.repository.UserRepository
import javax.inject.Inject

class UpdateSelectedScreenUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(uid: String, selectedScreen: SelectedScreen) =
        userRepository.updateSelectedScreen(uid,selectedScreen)
}