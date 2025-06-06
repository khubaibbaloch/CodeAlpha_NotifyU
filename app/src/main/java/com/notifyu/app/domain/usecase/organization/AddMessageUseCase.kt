package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.data.model.Organization
import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class AddMessageUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    suspend operator fun invoke(content: String, senderId: String, currentOrgId: String) =
        organizationRepository.addMessage(
            content = content,
            senderId = senderId,
            currentOrgId = currentOrgId
        )
}