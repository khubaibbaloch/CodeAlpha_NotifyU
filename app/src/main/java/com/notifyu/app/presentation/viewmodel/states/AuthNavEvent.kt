package com.notifyu.app.presentation.viewmodel.states


sealed class AuthNavEvent {
    object None : AuthNavEvent()
    object ToHome : AuthNavEvent()
    object ToLogin : AuthNavEvent()
    object ToVerifyEmail : AuthNavEvent()
    object ToOrganizationOwned : AuthNavEvent()
    object ToOrganizationJoined : AuthNavEvent()
}
