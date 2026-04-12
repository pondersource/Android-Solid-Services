package com.pondersource.androidsolidservices.di

import android.accounts.AccountManager
import android.content.Context
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.solidandroidapi.Authenticator
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModule
import com.pondersource.solidandroidapi.SolidResourceManager
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
        return Authenticator.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSolidResourceManager(
        @ApplicationContext context: Context,
    ): SolidResourceManager {
        return SolidResourceManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSolidContactsDataModule(
        @ApplicationContext context: Context,
    ): SolidContactsDataModule {
        return SolidContactsDataModule.getInstance(context)
    }
}