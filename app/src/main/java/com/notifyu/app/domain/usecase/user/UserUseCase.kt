package com.notifyu.app.domain.usecase.user

data class UserUseCase(
    val createUser: CreateUserUseCase,
    val setSelectedScreen : UpdateSelectedScreenUseCase,
    val getSelectedScreen : FetchSelectedScreenForCurrentUserUseCase,
)