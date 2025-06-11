package com.notifyu.app.domain.usecase.notification.data

import com.notifyu.app.domain.usecase.notification.SendFcmPushNotificationUseCase
import com.notifyu.app.domain.usecase.notification.SyncFcmTokenIfChangedUseCase

data class NotificationUseCase(
    val syncFcmToken: SyncFcmTokenIfChangedUseCase,
    val sendPushNotification: SendFcmPushNotificationUseCase,
)