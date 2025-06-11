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
import com.notifyu.app.domain.usecase.auth.data.AuthUseCases
import com.notifyu.app.domain.usecase.auth.CheckEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.LoginWithEmailUserCase
import com.notifyu.app.domain.usecase.auth.ObserveAuthStateUseCase
import com.notifyu.app.domain.usecase.auth.SendEmailVerificationUseCase
import com.notifyu.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.notifyu.app.domain.usecase.auth.SignOutUseCase
import com.notifyu.app.domain.usecase.auth.SignUpUseCase
import com.notifyu.app.domain.usecase.auth.UpdatePasswordUseCase
import com.notifyu.app.domain.usecase.notification.data.NotificationUseCase
import com.notifyu.app.domain.usecase.organization.AddMessageUseCase
import com.notifyu.app.domain.usecase.organization.AddOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.FetchMemberOrganizationsUseCase
import com.notifyu.app.domain.usecase.organization.FetchMessagesForOrganizationUseCase
import com.notifyu.app.domain.usecase.organization.FetchOwnedOrganizationsUseCase
import com.notifyu.app.domain.usecase.organization.FetchUsersByIdsUseCase
import com.notifyu.app.domain.usecase.organization.JoinOrganizationByNameAndCodeUseCase
import com.notifyu.app.domain.usecase.organization.data.OrganizationUseCase
import com.notifyu.app.domain.usecase.organization.RemoveMemberFromOrganizationUseCase
import com.notifyu.app.domain.usecase.notification.SendFcmPushNotificationUseCase
import com.notifyu.app.domain.usecase.notification.SyncFcmTokenIfChangedUseCase
import com.notifyu.app.domain.usecase.organization.UpdateSeenByForLastMessageUseCase
import com.notifyu.app.domain.usecase.organization.UpdateOrganizationAvatarIndexUseCase
import com.notifyu.app.domain.usecase.user.CreateUserUseCase
import com.notifyu.app.domain.usecase.user.data.UserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Marks this object as a Dagger Hilt module where dependencies will be provided
@Module
@InstallIn(SingletonComponent::class) // Tells Hilt to install this module in the SingletonComponent (i.e., app-wide singletons)
object AppModule {

    // Provides a singleton instance of FirebaseAuth
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Provides a singleton instance of FirebaseFirestore (cloud database)
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Provides a singleton instance of FirebaseMessaging (for push notifications)
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    // Provides an implementation of AuthRepository using FirebaseAuth
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth)

    // Provides AuthUseCases, each wrapping a specific authentication-related use case
    @Provides
    @Singleton
    fun provideAuthUseCases(repository: AuthRepository): AuthUseCases {
        return AuthUseCases(
            signUp = SignUpUseCase(repository), // Handles user registration
            login = LoginWithEmailUserCase(repository), // Handles login with email and password
            sendEmailVerification = SendEmailVerificationUseCase(repository), // Sends email verification
            checkEmailVerification = CheckEmailVerificationUseCase(repository), // Checks if email is verified
            sendPasswordReset = SendPasswordResetEmailUseCase(repository), // Sends password reset email
            updatePassword = UpdatePasswordUseCase(repository), // Updates user password
            signOutUseCase = SignOutUseCase(repository), // Handles sign out
            authStateUseCase = ObserveAuthStateUseCase(repository) // Observes the current auth state
        )
    }

    // Provides an implementation of OrganizationRepository using FirebaseAuth, Firestore, and Messaging
    @Provides
    @Singleton
    fun provideOrganizationRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseMessaging: FirebaseMessaging,
    ): OrganizationRepository =
        OrganizationRepositoryImpl(firebaseAuth, firestore, firebaseMessaging)

    // Provides OrganizationUseCase, a collection of use cases related to organization functionality
    @Provides
    @Singleton
    fun provideOrganizationUseCase(repository: OrganizationRepository): OrganizationUseCase {
        return OrganizationUseCase(
            addOrg = AddOrganizationUseCase(repository), // Adds a new organization
            addMsg = AddMessageUseCase(repository), // Adds a message to an organization
            getOwnedOrgs = FetchOwnedOrganizationsUseCase(repository), // Fetches organizations owned by the user
            getOrgMessages = FetchMessagesForOrganizationUseCase(repository), // Fetches messages for a specific organization
            joinOrg = JoinOrganizationByNameAndCodeUseCase(repository), // Join an organization using name + code
            getMemberOrgs = FetchMemberOrganizationsUseCase(repository), // Fetches organizations the user is a member of
            getUsersByIds = FetchUsersByIdsUseCase(repository), // Gets user info by list of user IDs
            removeMember = RemoveMemberFromOrganizationUseCase(repository), // Removes a member from an organization
            updateAvatarIndex = UpdateOrganizationAvatarIndexUseCase(repository), // Updates organization's avatar
            updateSeenByForLastMessage = UpdateSeenByForLastMessageUseCase(repository) // Updates the seen-by list for the latest message
        )
    }

    // Provides an implementation of UserRepository using FirebaseAuth and Firestore
    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
    ): UserRepository =
        UserRepositoryImpl(firebaseAuth, firestore)

    // Provides UserUseCase for managing user-related operations
    @Provides
    @Singleton
    fun provideUserUseCase(repository: UserRepository): UserUseCase {
        return UserUseCase(
            createUser = CreateUserUseCase(repository), // Creates a new user profile
        )
    }

    // Provides NotificationRepository using FirebaseAuth, Firestore, and FirebaseMessaging
    @Provides
    @Singleton
    fun provideNotificationRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseMessaging: FirebaseMessaging
    ): NotificationRepository =
        NotificationRepositoryImpl(firebaseAuth, firestore,firebaseMessaging)

    // Provides NotificationUseCase for managing push notification use cases
    @Provides
    @Singleton
    fun provideNotificationUseCase(repository: NotificationRepository): NotificationUseCase {
        return NotificationUseCase(
            syncFcmToken = SyncFcmTokenIfChangedUseCase(repository), // Syncs FCM token if changed
            sendPushNotification = SendFcmPushNotificationUseCase(repository), // Sends push notifications using FCM
        )
    }
}
