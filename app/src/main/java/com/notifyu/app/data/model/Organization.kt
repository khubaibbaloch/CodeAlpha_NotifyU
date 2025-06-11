package com.notifyu.app.data.model



// Data class representing an organization
data class Organization(
    val id: String = "",                       // Unique ID for the organization
    val name: String = "",                     // Name of the organization
    val code: String = "",                     // A special code (possibly for joining or identifying the org)
    val owner: String = "",                    // ID of the owner/creator of the organization
    val avatarIndex :Int = 0,                  // Index pointing to a specific avatar/image for the organization
    val members: List<String> = emptyList(),   // List of user IDs who are members of the organization
    val messages: List<Message> = emptyList(), // List of messages in the organization (may grow large)
    val lastMessage: LastMessage? = null       // Most recent message metadata (nullable if no messages yet)
)

// Data class representing a single message
data class Message(
    val content: String = "",                      // Text/content of the message
    val senderId: String = "",                     // ID of the user who sent the message
    val timestamp: Long = System.currentTimeMillis(), // Time the message was sent, defaults to current time
)

// Data class representing the last message info, typically used for previews or notifications
data class LastMessage(
    val content: String = "",                      // Content of the last message
    val senderId: String = "",                     // ID of the sender of the last message
    val timestamp: Long = System.currentTimeMillis(), // Timestamp of when the last message was sent
    val seenBy: List<String> = emptyList()         // List of user IDs who have seen this message
)





