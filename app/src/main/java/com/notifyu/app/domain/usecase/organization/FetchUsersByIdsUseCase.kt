package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class FetchUsersByIdsUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    suspend operator fun invoke(userIds: List<String>) =
        organizationRepository.fetchUsersByIds(userIds = userIds)
}