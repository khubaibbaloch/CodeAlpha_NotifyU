package com.notifyu.app.domain.usecase.notification

data class NotificationUseCase(
    val syncFcmToken: SyncFcmTokenIfChangedUseCase,
    val sendPushNotification: SendFcmPushNotificationUseCase,
)