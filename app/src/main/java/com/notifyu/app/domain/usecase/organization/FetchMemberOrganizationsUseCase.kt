package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.data.model.Organization
import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class FetchMemberOrganizationsUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    operator fun invoke(onUpdate: (List<Organization>) -> Unit) =
        organizationRepository.fetchMemberOrganizations(onUpdate)
}