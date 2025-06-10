package com.notifyu.app.domain.usecase.organization

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
