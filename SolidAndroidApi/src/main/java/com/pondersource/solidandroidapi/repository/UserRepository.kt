package com.pondersource.solidandroidapi.repository

import com.pondersource.shared.data.Profile

interface UserRepository {

    fun readProfile(): Profile

    fun writeProfile(profile: Profile)
}