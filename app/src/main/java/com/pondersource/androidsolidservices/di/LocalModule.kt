package com.pondersource.androidsolidservices.di

import android.accounts.AccountManager
import android.content.Context
import com.pondersource.androidsolidservices.usecase.Authenticator
import com.pondersource.androidsolidservices.usecase.AuthenticatorImplementation
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.androidsolidservices.usecase.SolidResourceManager
import com.pondersource.androidsolidservices.usecase.SolidResourceManagerImplementation
import com.pondersource.androidsolidservices.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthorizationService
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalModule {

    @Provides
    @Singleton
    @Named(Constants.ASS_ACCOUNT_NAME)
    fun provideASSAccountName(): String = "com.pondersource.androidsolidservices.account.DEMOACCOUNT"

    @Provides
    @Singleton
    fun provideAccountManager(
        @ApplicationContext context: Context
    ): AccountManager {
        return AccountManager.get(context)
    }

    @Provides
    @Singleton
    fun provideAuthorizationService(
        @ApplicationContext context: Context
    ): AuthorizationService {
        return AuthorizationService(context)
    }

    @Provides
    @Singleton
    fun provideAuthenticator(
        userRepository: UserRepository,
        authService: AuthorizationService,
    ): Authenticator {
        return AuthenticatorImplementation(
            userRepository,
            authService,
        )
    }

    @Provides
    @Singleton
    fun provideSolidResourceManager(
        authenticator: Authenticator,
    ): SolidResourceManager {
        return SolidResourceManagerImplementation(
            authenticator
        )
    }
}