package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.toHouse
import com.pollyannawu.justwoo.datasource.AuthDataSource
import com.pollyannawu.justwoo.datasource.HouseDataSource
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.network.HouseApiService
import kotlinx.coroutines.flow.Flow


interface HouseRepository {
   fun observeHouses(): Flow<List<House>>
   suspend fun refreshHouses()
    suspend fun createHouse(house: House)

    suspend fun updateHouse(house: House)
}

class DefaultHouseRepository(
    private val authDataSource: AuthDataSource,
    private val houseApiService: HouseApiService,
    private val houseDataSource: HouseDataSource,

    ) : HouseRepository {

    override fun observeHouses(): Flow<List<House>> = houseDataSource.getHouses()
    override suspend fun refreshHouses() {
        val user = authDataSource.getUser() ?: return
        val result = houseApiService.getHouses(user.id)
        if (result is ApiResult.Success) {
            val houses = result.data.map { it.toHouse() }
            houseDataSource.updateHouses(houses)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun createHouse(house: House){
        val user = authDataSource.getUser() ?: return
        val result = houseApiService.createHouse(user.id, house)
        if (result is ApiResult.Success) {
            val house = result.data.toHouse()
            houseDataSource.updateHouse(house)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun updateHouse(house: House) {
        val user = authDataSource.getUser() ?: return
        val result = houseApiService.updateHouse(houseId = house.id, user.id)
        if (result is ApiResult.Success) {
            val house = result.data.toHouse()
            houseDataSource.updateHouse(house)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }
}