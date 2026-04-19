package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.datasource.AuthDataSource
import com.pollyannawu.justwoo.datasource.HouseDataSource
import com.pollyannawu.justwoo.model.HouseDataResult
import kotlinx.coroutines.flow.Flow


interface HouseRepository {
    suspend fun getHouses(): Flow<HouseDataResult>
    suspend fun createHouse(house: House): Flow<HouseDataResult>
    suspend fun updateHouse(house: House): Flow<HouseDataResult>
    suspend fun deleteHouse(id: Long)
}

class DefaultHouseRepository(
    private val authDataSource: AuthDataSource,
    private val houseDataSource: HouseDataSource,

) : HouseRepository {
    // TODO: still working on
    override suspend fun getHouses(): Flow<HouseDataResult> {
        TODO("Not yet implemented")
    }

    override suspend fun createHouse(house: House): Flow<HouseDataResult> {
        TODO("Not yet implemented")
    }

    override suspend fun updateHouse(house: House): Flow<HouseDataResult> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHouse(id: Long) {
        TODO("Not yet implemented")
    }

}