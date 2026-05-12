package com.erfangholami.androidsolidservices.di

import com.erfangholami.androidsolidservices.repository.AccessGrantRepository
import com.erfangholami.androidsolidservices.repository.AccessGrantRepositoryImplementation
import com.erfangholami.androidsolidservices.repository.ResourcePermissionRepository
import com.erfangholami.androidsolidservices.repository.ResourcePermissionRepositoryImplementation
import com.erfangholami.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

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