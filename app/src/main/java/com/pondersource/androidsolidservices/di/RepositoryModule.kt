package com.pondersource.androidsolidservices.di

import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import com.pondersource.androidsolidservices.repository.AccessGrantRepositoryImplementation
import com.pondersource.androidsolidservices.repository.ResourcePermissionRepository
import com.pondersource.androidsolidservices.repository.ResourcePermissionRepositoryImplementation
import com.pondersource.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSource
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