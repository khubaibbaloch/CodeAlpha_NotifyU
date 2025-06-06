package com.notifyu.app.domain.usecase.notification

import android.content.Context
import com.notifyu.app.domain.repository.NotificationRepository
import com.notifyu.app.domain.repository.OrganizationRepository
import javax.inject.Inject

class SendFcmPushNotificationUseCase @Inject constructor(private val notificationRepository: NotificationRepository) {
    suspend operator fun invoke(context: Context, targetTokens: List<String>, title: String, body: String) =
       notificationRepository.sendFcmPushNotification(context = context,targetTokens=targetTokens,title=title,body=body)
}