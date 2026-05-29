package com.pollyannawu.justwoo.android.ui.home

import androidx.lifecycle.ViewModel
import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.datasource.auth.UserStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class HouseInfoViewModel(
    private val houseRepository: HouseRepository,
    private val userStorage: UserStorage,
) : ViewModel() {

    val currentUserId: Flow<Long?> = userStorage.userFlow.map { it?.id }

    val house: Flow<House?> = userStorage.houseIdFlow.flatMapLatest { houseId ->
        houseRepository.observeHouses().map { houses ->
            houses.firstOrNull { it.id == houseId }
        }
    }
}
