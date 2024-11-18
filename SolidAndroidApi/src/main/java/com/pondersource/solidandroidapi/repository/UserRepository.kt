package com.pondersource.solidandroidapi.repository

import com.pondersource.solidandroidclient.data.Profile

interface UserRepository {

    fun readProfile(): Profile

    fun writeProfile(profile: Profile)
}