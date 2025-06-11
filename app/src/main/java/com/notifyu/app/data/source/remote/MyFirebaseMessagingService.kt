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

// This class extends FirebaseMessagingService to handle FCM (Firebase Cloud Messaging) events
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // A map to store up to 5 latest messages per organization (keyed by orgName)
    private val orgMessagesMap = mutableMapOf<String, MutableList<String>>()

    // Called when a new FCM message is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract the orgName from the message data; if null, exit early
        val orgName = remoteMessage.data["orgName"] ?: return

        // Get the title and body from the notification payload, with default values
        val title = remoteMessage.notification?.title ?: "New message"
        val body = remoteMessage.notification?.body ?: "No content"

        // Construct the message to display; originally was using both title and body
        //val message = "$title - $body"
        val message = body // Only using body now

        // Retrieve or create a message list for the organization
        val messages = orgMessagesMap.getOrPut(orgName) { mutableListOf() }

        // Ensure the list contains only the last 5 messages
        if (messages.size >= 5) {
            messages.removeAt(0) // Remove the oldest message
        }
        messages.add(message) // Add the new message

        // Show the notification with all collected messages
        showOrgNotification(orgName, messages)
    }

    // Called when a new FCM registration token is generated
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Get the current Firebase authenticated user's UID
        val uid = Firebase.auth.currentUser?.uid

        // If user is logged in, update their FCM token in Firestore
        if (uid != null) {
            Firebase.firestore.collection("users")
                .document(uid)
                .update("fcmToken", token) // Update the token field
                .addOnSuccessListener {
                    Log.d("FCM", "Token updated successfully") // Success log
                }
                .addOnFailureListener {
                    Log.e("FCM", "Failed to update FCM token", it) // Error log
                }
        }
    }

    // Shows a grouped notification for the given organization
    private fun showOrgNotification(orgName: String, messages: List<String>) {
        val channelId = "${orgName}_channel" // Unique channel ID per org
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // If the device runs Android O or newer, create a notification channel (if not exists)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Messages from $orgName", // User-visible channel name
                    NotificationManager.IMPORTANCE_HIGH // High importance to show heads-up notification
                )
                manager.createNotificationChannel(channel) // Register the channel
            }
        }

        // Create an intent to open the app when the notification is tapped
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Flags for security and reuse
        )

        // Build an inbox-style notification (shows each message as a line)
        val inboxStyle = NotificationCompat.InboxStyle()
        for (msg in messages) {
            inboxStyle.addLine(msg) // Add each message to the style
        }

        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // Notification icon
            .setContentTitle(orgName) // Title shown in notification
            .setContentText("${messages.size} message(s)") // Summary text
            .setStyle(inboxStyle) // Set inbox style for multiple messages
            .setContentIntent(pendingIntent) // Intent to open app on tap
            .setOnlyAlertOnce(true) // Prevent repeated sounds for updates
            .setAutoCancel(true) // Dismiss notification when tapped
            .build()

        // Show the notification using a unique ID (orgName's hash)
        manager.notify(orgName.hashCode(), notification)
    }
}
