package com.notifyu.app.domain.usecase.organization

import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class JoinOrganizationByNameAndCodeUseCase @Inject constructor(private val organizationRepository: OrganizationRepository) {
    suspend operator fun invoke(name: String, code: String) =
        organizationRepository.joinOrganizationByNameAndCode(name = name, code = code)
}