package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.dto.HouseRequest
import com.pollyannawu.justwoo.core.toHouse
import com.pollyannawu.justwoo.data.datasource.auth.UserStorage
import com.pollyannawu.justwoo.data.datasource.HouseDataSource
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.data.network.service.HouseApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

interface HouseRepository {
    fun observeHouses(): Flow<List<House>>
    suspend fun refreshHouses(page: Int = 1)
    suspend fun createHouse(house: House)
    suspend fun updateHouse(house: House)
    suspend fun addMember(houseId: Long, memberId: Long)
    suspend fun removeMember(houseId: Long, memberId: Long)
}

class DefaultHouseRepository(
    private val userStorage: UserStorage,
    private val houseApiService: HouseApiService,
    private val houseDataSource: HouseDataSource,
) : HouseRepository {

    override fun observeHouses(): Flow<List<House>> = houseDataSource.getHouses()

    override suspend fun refreshHouses(page: Int) {
        val result = houseApiService.getHouses(page)
        if (result is ApiResult.Success) {
            val houses = result.data.content.map { it.toHouse() }
            houseDataSource.updateHouses(houses)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun createHouse(house: House) {
        val user = userStorage.getUser() ?: return
        val now = Clock.System.now()
        val result = houseApiService.createHouse(
            HouseRequest(
                title = house.name,
                name = house.name,
                adminUserId = user.id,
                memberIds = house.members.map { it.userId },
                description = house.description,
                avatar = house.avatar,
                createTime = now,
                updateTime = now,
            )
        )
        if (result is ApiResult.Success) {
            houseDataSource.updateHouse(result.data.toHouse())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun updateHouse(house: House) {
        val user = userStorage.getUser() ?: return
        val now = Clock.System.now()
        val result = houseApiService.updateHouse(
            houseId = house.id,
            request = HouseRequest(
                title = house.name,
                name = house.name,
                adminUserId = user.id,
                memberIds = house.members.map { it.userId },
                description = house.description,
                avatar = house.avatar,
                createTime = house.createTime,
                updateTime = now,
            )
        )
        if (result is ApiResult.Success) {
            houseDataSource.updateHouse(result.data.toHouse())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun addMember(houseId: Long, memberId: Long) {
        val result = houseApiService.addMember(houseId, memberId)
        if (result is ApiResult.Success) {
            houseDataSource.updateHouse(result.data.toHouse())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun removeMember(houseId: Long, memberId: Long) {
        val result = houseApiService.removeMember(houseId, memberId)
        if (result is ApiResult.Success) {
            houseDataSource.updateHouse(result.data.toHouse())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }
}
