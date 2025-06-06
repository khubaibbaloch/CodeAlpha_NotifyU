package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class RemoveMemberFromOrganizationUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    suspend operator fun invoke(organizationId: String, uidToRemove: String) =
        organizationRepository.removeMemberFromOrganization(organizationId,uidToRemove)
}