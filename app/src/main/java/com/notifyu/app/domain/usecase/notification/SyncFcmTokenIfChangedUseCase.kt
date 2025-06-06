package com.notifyu.app.domain.usecase.notification

import com.notifyu.app.domain.repository.NotificationRepository
import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class SyncFcmTokenIfChangedUseCase @Inject constructor(private val notificationRepository: NotificationRepository) {
    suspend operator fun invoke() = notificationRepository.syncFcmTokenIfChanged()
}