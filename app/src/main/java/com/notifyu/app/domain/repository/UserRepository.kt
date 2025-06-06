package com.notifyu.app.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.notifyu.app.data.model.SelectedScreen

interface UserRepository {
    suspend fun createUser(user: FirebaseUser): Result<String>
    suspend fun updateSelectedScreen(uid: String, selectedScreen: SelectedScreen): Result<String>
    fun fetchSelectedScreenForCurrentUser(onResult: (String?) -> Unit)
}