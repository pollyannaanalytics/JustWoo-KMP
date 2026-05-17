package com.pollyannawu.justwoo.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.data.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class MainViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUserId: StateFlow<Long?> = authRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val currentHouseId: StateFlow<Long?> = authRepository.currentHouseId
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
