package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.data.model.Message
import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class FetchMessagesForOrganizationUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    operator fun invoke(orgId: String, onUpdate: (List<Message>) -> Unit) =
        organizationRepository.fetchMessagesForOrganization(orgId = orgId, onUpdate = onUpdate)
}