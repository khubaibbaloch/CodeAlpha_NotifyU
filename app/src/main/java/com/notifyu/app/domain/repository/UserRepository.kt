package com.notifyu.app.domain.repository

import com.google.firebase.auth.FirebaseUser


interface UserRepository {
    suspend fun createUser(user: FirebaseUser): Result<String>
}