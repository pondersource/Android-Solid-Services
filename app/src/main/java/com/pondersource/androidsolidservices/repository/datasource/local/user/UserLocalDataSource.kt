package com.pondersource.androidsolidservices.repository.datasource.local.user

import com.pondersource.solidandroidclient.data.Profile

interface UserLocalDataSource {

    fun readProfile(): Profile

    fun writeProfile(profile: Profile)
}