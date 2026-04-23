package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.model.ApiResult


interface HouseApiService {
    suspend fun getHouses(userId: Long): ApiResult<List<HouseResponse>>
    suspend fun createHouse(userId: Long, house: House): ApiResult<HouseResponse>
    suspend fun updateHouse(houseId: Long, userId: Long): ApiResult<HouseResponse>
    suspend fun getHouseById(id: Long, userId: Long): ApiResult<HouseResponse>
}