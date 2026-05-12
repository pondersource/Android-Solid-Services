package com.erfangholami.androidsolidservices.di

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.erfangholami.androidsolidservices.base.Constants
import com.erfangholami.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSource
import com.erfangholami.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSourceImplementation
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

    companion object {
        private const val PREFERENCES_NAME = "com.erfangholami.androidsolidservices.preferences"
    }

    private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        PREFERENCES_NAME
    )

    @Provides
    @Singleton
    @Named(Constants.ASS_SHARED_PREFERENCES_NAME)
    fun provideSharedPreferencesName(): String = "solid_android_auth"

    @Provides
    @Singleton
    fun providePreferencesDatasource(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.preferencesDataStore

    @Provides
    fun provideAccessGrantLocalDataSource(
        dataStore: DataStore<Preferences>,
    ): AccessGrantLocalDataSource {
        return AccessGrantLocalDataSourceImplementation(dataStore)
    }
}