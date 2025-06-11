package com.notifyu.app.domain.usecase.auth.data

import com.notifyu.app.domain.usecase.auth.CheckEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.LoginWithEmailUserCase
import com.notifyu.app.domain.usecase.auth.ObserveAuthStateUseCase
import com.notifyu.app.domain.usecase.auth.SendEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.notifyu.app.domain.usecase.auth.SignOutUseCase
import com.notifyu.app.domain.usecase.auth.SignUpUseCase
import com.notifyu.app.domain.usecase.auth.UpdatePasswordUseCase

data class AuthUseCases(
    val signUp: SignUpUseCase,
    val login: LoginWithEmailUserCase,
    val sendEmailVerification: SendEmailVerificationUseCase,
    val checkEmailVerification: CheckEmailVerificationUseCase,
    val sendPasswordReset: SendPasswordResetEmailUseCase,
    val updatePassword: UpdatePasswordUseCase,
    val signOutUseCase: SignOutUseCase,
    val authStateUseCase: ObserveAuthStateUseCase
)