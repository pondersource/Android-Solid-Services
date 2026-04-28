package com.pondersource.solidandroidapi

import com.pondersource.shared.domain.profile.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class InProgressAuthStore {
    private val _state = MutableStateFlow<Profile?>(null)
    val state: StateFlow<Profile?> = _state.asStateFlow()

    fun get(): Profile? = _state.value
    fun set(profile: Profile) { _state.value = profile }
    fun clear() { _state.value = null }
}
