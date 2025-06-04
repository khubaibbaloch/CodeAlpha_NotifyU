package com.notifyu.app.data.source.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            val userDocRef = Firebase.firestore.collection("users").document(uid)

            // Try to update the fcmToken field
            userDocRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Update failed, trying to create user document", e)
                }
        }
    }



    private fun showNotification(title: String?, body: String?) {
        val builder = NotificationCompat.Builder(this, "org_channel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "org_channel", "Org Notifications", NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, builder.build())
    }
}
