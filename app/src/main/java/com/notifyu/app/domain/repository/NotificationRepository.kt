package com.notifyu.app.domain.repository

import android.content.Context

interface NotificationRepository {
    suspend fun syncFcmTokenIfChanged(): Result<String>
    suspend fun sendFcmPushNotification(context: Context, targetTokens: List<String>, title: String, body: String): Result<Unit>
}