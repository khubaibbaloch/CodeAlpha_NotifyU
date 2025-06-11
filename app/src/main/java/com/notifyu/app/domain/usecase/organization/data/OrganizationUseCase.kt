package com.notifyu.app.domain.usecase.organization.data

import com.notifyu.app.domain.usecase.organization.AddMessageUseCase
import com.notifyu.app.domain.usecase.organization.AddOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.FetchMemberOrganizationsUseCase
import com.notifyu.app.domain.usecase.organization.FetchMessagesForOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.FetchOwnedOrganizationsUseCase
import com.notifyu.app.domain.usecase.organization.FetchUsersByIdsUseCase
import com.notifyu.app.domain.usecase.organization.JoinOrganizationByNameAndCodeUseCase
import com.notifyu.app.domain.usecase.organization.RemoveMemberFromOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.UpdateOrganizationAvatarIndexUseCase
import com.notifyu.app.domain.usecase.organization.UpdateSeenByForLastMessageUseCase

data class OrganizationUseCase(
    val addOrg: AddOrganizationUseCase,
    val addMsg: AddMessageUseCase,
    val getOwnedOrgs: FetchOwnedOrganizationsUseCase,
    val getOrgMessages: FetchMessagesForOrganizationUseCase,
    val joinOrg: JoinOrganizationByNameAndCodeUseCase,
    val getMemberOrgs: FetchMemberOrganizationsUseCase,
    val getUsersByIds: FetchUsersByIdsUseCase,
    val removeMember: RemoveMemberFromOrganizationUseCase,
    val updateAvatarIndex: UpdateOrganizationAvatarIndexUseCase,
    val updateSeenByForLastMessage: UpdateSeenByForLastMessageUseCase
)