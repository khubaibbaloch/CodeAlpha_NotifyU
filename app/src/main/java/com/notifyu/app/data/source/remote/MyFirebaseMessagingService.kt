package com.notifyu.app.data.source.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.notifyu.app.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Stores up to 5 latest messages per org
    private val orgMessagesMap = mutableMapOf<String, MutableList<String>>()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val orgName = remoteMessage.data["orgName"] ?: return
        val title = remoteMessage.notification?.title ?: "New message"
        val body = remoteMessage.notification?.body ?: "No content"

        //val message = "$title - $body"
        val message = body

        val messages = orgMessagesMap.getOrPut(orgName) { mutableListOf() }

        // Keep only the last 5 messages
        if (messages.size >= 5) {
            messages.removeAt(0)
        }
        messages.add(message)

        showOrgNotification(orgName, messages)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users")
                .document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token updated successfully")
                }
                .addOnFailureListener {
                    Log.e("FCM", "Failed to update FCM token", it)
                }
        }
    }

    private fun showOrgNotification(orgName: String, messages: List<String>) {
        val channelId = "${orgName}_channel"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create a separate channel for each org
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Messages from $orgName",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }
        }

        // Create an intent to open the app when the notification is clicked
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
        for (msg in messages) {
            inboxStyle.addLine(msg)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(orgName)
            .setContentText("${messages.size} message(s)")
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .build()

        manager.notify(orgName.hashCode(), notification)
    }
}