package com.notifyu.app.domain.usecase.auth

data class AuthUseCases(
    val signUp: SignUpUseCase,
    val login: LoginWithEmailUserCase,
    val sendEmailVerification: SendEmailVerificationUseCase,
    val checkEmailVerification: CheckEmailVerificationUseCase,
    val sendPasswordReset: SendPasswordResetEmailUseCase,
    val updatePassword: UpdatePasswordUseCase,
)
