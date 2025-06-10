package com.notifyu.app.di


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.notifyu.app.data.repository.AuthRepositoryImpl
import com.notifyu.app.data.repository.NotificationRepositoryImpl
import com.notifyu.app.data.repository.OrganizationRepositoryImpl
import com.notifyu.app.data.repository.UserRepositoryImpl
import com.notifyu.app.domain.repository.AuthRepository
import com.notifyu.app.domain.repository.NotificationRepository
import com.notifyu.app.domain.repository.OrganizationRepository
import com.notifyu.app.domain.repository.UserRepository
import com.notifyu.app.domain.usecase.auth.AuthUseCases
import com.notifyu.app.domain.usecase.auth.CheckEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.LoginWithEmailUserCase
import com.notifyu.app.domain.usecase.auth.ObserveAuthStateUseCase
import com.notifyu.app.domain.usecase.auth.SendEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.notifyu.app.domain.usecase.auth.SignOutUseCase
import com.notifyu.app.domain.usecase.auth.SignUpUseCase
import com.notifyu.app.domain.usecase.auth.UpdatePasswordUseCase
import com.notifyu.app.domain.usecase.notification.NotificationUseCase
import com.notifyu.app.domain.usecase.organization.AddMessageUseCase
import com.notifyu.app.domain.usecase.organization.AddOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.FetchMemberOrganizationsUseCase
import com.notifyu.app.domain.usecase.organization.FetchMessagesForOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.FetchOwnedOrganizationsUseCase
import com.notifyu.app.domain.usecase.organization.FetchUsersByIdsUseCase
import com.notifyu.app.domain.usecase.organization.JoinOrganizationByNameAndCodeUseCase
import com.notifyu.app.domain.usecase.organization.OrganizationUseCase
import com.notifyu.app.domain.usecase.organization.RemoveMemberFromOrganizationUseCase
import com.notifyu.app.domain.usecase.notification.SendFcmPushNotificationUseCase
import com.notifyu.app.domain.usecase.notification.SyncFcmTokenIfChangedUseCase
import com.notifyu.app.domain.usecase.organization.UpdateSeenByForLastMessageUseCase
import com.notifyu.app.domain.usecase.organization.UpdateOrganizationAvatarIndexUseCase
import com.notifyu.app.domain.usecase.user.CreateUserUseCase
import com.notifyu.app.domain.usecase.user.GetCurrentUserUseCase
import com.notifyu.app.domain.usecase.user.UserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth)

    @Provides
    @Singleton
    fun provideAuthUseCases(repository: AuthRepository): AuthUseCases {
        return AuthUseCases(
            signUp = SignUpUseCase(repository),
            login = LoginWithEmailUserCase(repository),
            sendEmailVerification = SendEmailVerificationUseCase(repository),
            checkEmailVerification = CheckEmailVerificationUseCase(repository),
            sendPasswordReset = SendPasswordResetEmailUseCase(repository),
            updatePassword = UpdatePasswordUseCase(repository),
            signOutUseCase = SignOutUseCase(repository),
            authStateUseCase = ObserveAuthStateUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideOrganizationRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseMessaging: FirebaseMessaging,
    ): OrganizationRepository =
        OrganizationRepositoryImpl(firebaseAuth, firestore, firebaseMessaging)

    @Provides
    @Singleton
    fun provideOrganizationUseCase(repository: OrganizationRepository): OrganizationUseCase {
        return OrganizationUseCase(
            addOrg = AddOrganizationUseCase(repository),
            addMsg = AddMessageUseCase(repository),
            getOwnedOrgs = FetchOwnedOrganizationsUseCase(repository),
            getOrgMessages = FetchMessagesForOrganizationUseCase(repository),
            joinOrg = JoinOrganizationByNameAndCodeUseCase(repository),
            getMemberOrgs = FetchMemberOrganizationsUseCase(repository),
            getUsersByIds = FetchUsersByIdsUseCase(repository),
            removeMember = RemoveMemberFromOrganizationUseCase(repository),
            updateAvatarIndex = UpdateOrganizationAvatarIndexUseCase(repository),
            updateSeenByForLastMessage = UpdateSeenByForLastMessageUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
    ): UserRepository =
        UserRepositoryImpl(firebaseAuth, firestore)

    @Provides
    @Singleton
    fun provideUserUseCase(repository: UserRepository): UserUseCase {
        return UserUseCase(
            currentUser = GetCurrentUserUseCase(repository),
            createUser = CreateUserUseCase(repository),
        )
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseMessaging: FirebaseMessaging
    ): NotificationRepository =
        NotificationRepositoryImpl(firebaseAuth, firestore,firebaseMessaging)

    @Provides
    @Singleton
    fun provideNotificationUseCase(repository: NotificationRepository): NotificationUseCase {
        return NotificationUseCase(
            syncFcmToken = SyncFcmTokenIfChangedUseCase(repository),
            sendPushNotification = SendFcmPushNotificationUseCase(repository),
        )
    }
}
