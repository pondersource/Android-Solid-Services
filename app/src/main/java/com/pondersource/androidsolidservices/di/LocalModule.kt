package com.pondersource.androidsolidservices.di

import android.accounts.AccountManager
import android.content.Context
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.solidandroidapi.Authenticator
import com.pondersource.solidandroidapi.AuthenticatorImplementation
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModule
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModuleImplementation
import com.pondersource.solidandroidapi.SolidResourceManager
import com.pondersource.solidandroidapi.SolidResourceManagerImplementation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideAuthenticator(
        @ApplicationContext context: Context,
    ): Authenticator {
        return AuthenticatorImplementation.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSolidResourceManager(
        @ApplicationContext context: Context,
    ): SolidResourceManager {
        return SolidResourceManagerImplementation.getInstance(
            context
        )
    }

    @Provides
    @Singleton
    fun provideSolidContactsDataModule(
        @ApplicationContext context: Context,
    ): SolidContactsDataModule {
        return SolidContactsDataModuleImplementation.getInstance(
            context
        )
    }
}