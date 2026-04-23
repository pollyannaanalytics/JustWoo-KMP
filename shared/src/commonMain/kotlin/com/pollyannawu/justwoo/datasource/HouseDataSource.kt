package com.pollyannawu.justwoo.datasource

import com.pollyannawu.justwoo.core.House
import kotlinx.coroutines.flow.Flow

interface HouseDataSource {
    fun getHouses(): Flow<List<House>>
    suspend fun getHouseById(id: Long): House
    suspend fun createHouse(house: House)
    suspend fun updateHouse(house: House)
    suspend fun updateHouses(houses: List<House>)
    suspend fun deleteHouse(id: Long)
}