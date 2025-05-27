package com.notifyu.app.data.repository

import com.notifyu.app.data.model.Event

class FirebaseEventRepository : EventRepository {
    // Implements above methods using Firebase
    override suspend fun getEvents(): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun addEvent(event: Event) {
        TODO("Not yet implemented")
    }

    override suspend fun getUpcomingEvents(): List<Event> {
        TODO("Not yet implemented")
    }
}