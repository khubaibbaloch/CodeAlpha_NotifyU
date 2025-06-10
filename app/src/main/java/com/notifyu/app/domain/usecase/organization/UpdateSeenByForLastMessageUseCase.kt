package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.data.model.Message
import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class UpdateSeenByForLastMessageUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    suspend operator fun invoke(currentOrgId: String, currentUserUid: String, ) =
        organizationRepository.updateSeenByForLastMessage(currentOrgId, currentUserUid)
}