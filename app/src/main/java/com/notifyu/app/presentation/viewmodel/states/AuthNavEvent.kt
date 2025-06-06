package com.notifyu.app.presentation.viewmodel.states


sealed class AuthNavEvent {
    object None : AuthNavEvent()
    object ToHome : AuthNavEvent()
    object ToVerifyEmail : AuthNavEvent()
}
