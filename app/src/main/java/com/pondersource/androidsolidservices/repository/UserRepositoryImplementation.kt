package com.pondersource.androidsolidservices.repository

import com.pondersource.androidsolidservices.repository.datasource.local.user.UserLocalDataSource
import com.pondersource.solidandroidapi.repository.UserRepository
import com.pondersource.shared.data.Profile

class UserRepositoryImplementation(
    private val userLocalDataSource: UserLocalDataSource
): UserRepository {

    override fun readProfile(): Profile {
        return userLocalDataSource.readProfile()
    }

    override fun writeProfile(profile: Profile) {
        userLocalDataSource.writeProfile(profile)
    }

}