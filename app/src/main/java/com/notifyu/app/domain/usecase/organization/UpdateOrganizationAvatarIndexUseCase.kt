package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class UpdateOrganizationAvatarIndexUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    suspend operator fun invoke(orgId: String, newAvatarIndex: Int) =
        organizationRepository.updateOrganizationAvatarIndex(
            orgId = orgId,
            newAvatarIndex = newAvatarIndex
        )
}