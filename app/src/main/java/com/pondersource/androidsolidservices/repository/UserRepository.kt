package com.pondersource.androidsolidservices.repository

import com.pondersource.solidandroidclient.data.Profile

interface UserRepository {

    fun readProfile(): Profile

    fun writeProfile(profile: Profile)
}