package com.notifyu.app.data.repository

import com.notifyu.app.data.model.Event

interface EventRepository {
    suspend fun getEvents(): List<Event>
    suspend fun addEvent(event: Event)
    suspend fun getUpcomingEvents(): List<Event>
}
