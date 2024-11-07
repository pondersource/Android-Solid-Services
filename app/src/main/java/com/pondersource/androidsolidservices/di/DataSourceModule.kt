package com.pondersource.androidsolidservices.di

import android.content.Context
import android.content.SharedPreferences
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSource
import com.pondersource.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSourceImplementation
import com.pondersource.androidsolidservices.repository.datasource.local.user.UserLocalDataSource
import com.pondersource.androidsolidservices.repository.datasource.local.user.UserLocalDataSourceImplementation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {

    @Provides
    @Singleton
    @Named(Constants.ASS_SHARED_PREFERENCES_NAME)
    fun provideSharedPreferencesName(): String = "solid_android_auth"

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context,
        @Named(Constants.ASS_SHARED_PREFERENCES_NAME) sharedPreferencesName: String
    ): SharedPreferences {
        return context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    }

    @Provides
    fun provideUserLocalDataSource(
        sharedPreferences: SharedPreferences
    ): UserLocalDataSource {
        return UserLocalDataSourceImplementation(sharedPreferences)
    }

    @Provides
    fun provideAccessGrantLocalDataSource(
        sharedPreferences: SharedPreferences,
    ): AccessGrantLocalDataSource {
        return AccessGrantLocalDataSourceImplementation(sharedPreferences)
    }
}