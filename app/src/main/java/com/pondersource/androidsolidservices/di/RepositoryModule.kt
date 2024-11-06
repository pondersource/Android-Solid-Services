package com.pondersource.androidsolidservices.di

import com.pondersource.androidsolidservices.repository.UserRepository
import com.pondersource.androidsolidservices.repository.UserRepositoryImplementation
import com.pondersource.androidsolidservices.repository.datasource.local.user.UserLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideUserRepository(
        userLocalDataSource: UserLocalDataSource
    ): UserRepository {
        return UserRepositoryImplementation(userLocalDataSource)
    }
}