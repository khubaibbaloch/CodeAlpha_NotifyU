package com.notifyu.app.domain.repository

import android.content.Context
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.data.model.User


interface OrganizationRepository {
    suspend fun addOrganization(name: String, code: String): Result<String>
    suspend fun addMessage(content: String, senderId: String, currentOrgId: String): Result<String>
    fun fetchOwnedOrganizations(onUpdate: (List<Organization>) -> Unit)
    fun fetchMessagesForOrganization(orgId: String, onUpdate: (List<Message>) -> Unit)
    suspend fun joinOrganizationByNameAndCode(name: String, code: String): Result<String>
    fun fetchMemberOrganizations(onUpdate: (List<Organization>) -> Unit)
    suspend fun fetchUsersByIds(userIds: List<String>): Result<List<User>>
    suspend fun removeMemberFromOrganization(organizationId: String, uidToRemove: String): Result<String>
    suspend fun updateOrganizationAvatarIndex(orgId: String, newAvatarIndex: Int): Result<String>
}
