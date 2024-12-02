package com.pondersource.androidsolidservices.di

import android.content.Context
import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import com.pondersource.androidsolidservices.repository.AccessGrantRepositoryImplementation
import com.pondersource.androidsolidservices.repository.ResourcePermissionRepository
import com.pondersource.androidsolidservices.repository.ResourcePermissionRepositoryImplementation
import com.pondersource.solidandroidapi.repository.UserRepository
import com.pondersource.solidandroidapi.repository.UserRepositoryImplementation
import com.pondersource.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideUserRepository(
        @ApplicationContext context: Context,
    ): UserRepository {
        return UserRepositoryImplementation.getInstance(context)
    }

    @Provides
    fun provideAccessGrantRepository(
        accessGrantLocalDataSource: AccessGrantLocalDataSource
    ): AccessGrantRepository {
        return AccessGrantRepositoryImplementation(accessGrantLocalDataSource)
    }

    @Provides
    fun provideResourcePermissionRepository(
        accessGrantRepository: AccessGrantRepository,
    ): ResourcePermissionRepository {
        return ResourcePermissionRepositoryImplementation(accessGrantRepository)
    }
}