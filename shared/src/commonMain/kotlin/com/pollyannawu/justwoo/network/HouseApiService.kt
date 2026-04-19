package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.model.ApiResult


interface HouseApiService {
    suspend fun getHouses(): ApiResult<List<HouseResponse>>


}