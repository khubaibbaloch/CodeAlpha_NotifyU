package com.notifyu.app.domain.usecase.user

import com.notifyu.app.domain.usecase.auth.SignUpUseCase

data class UserUseCase(
    val currentUser : GetCurrentUserUseCase,
    val createUser: CreateUserUseCase,
)