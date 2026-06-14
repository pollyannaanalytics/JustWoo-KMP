package com.pollyannawu.justwoo.data.network.service

import com.pollyannawu.justwoo.core.dto.HouseRequest
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.core.dto.PageResponse
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

interface HouseApiService {
    suspend fun getHouses(page: Int = 1): ApiResult<PageResponse<HouseResponse>>
    suspend fun createHouse(request: HouseRequest): ApiResult<HouseResponse>
    suspend fun updateHouse(houseId: Long, request: HouseRequest): ApiResult<HouseResponse>
    suspend fun getHouseById(houseId: Long): ApiResult<HouseResponse>
    suspend fun addMember(houseId: Long, memberId: Long): ApiResult<HouseResponse>
    suspend fun removeMember(houseId: Long, memberId: Long): ApiResult<HouseResponse>
}

class DefaultHouseApiService(
    private val ktorClient: HttpClient,
) : HouseApiService {

    override suspend fun getHouses(page: Int): ApiResult<PageResponse<HouseResponse>> = safeApiCall(tag = "HouseApi.getHouses") {
        ktorClient.get("/houses") {
            parameter("page", page)
        }.body()
    }

    override suspend fun createHouse(request: HouseRequest): ApiResult<HouseResponse> = safeApiCall(tag = "HouseApi.createHouse") {
        ktorClient.post("/houses") {
            setBody(request)
        }.body()
    }

    override suspend fun updateHouse(houseId: Long, request: HouseRequest): ApiResult<HouseResponse> = safeApiCall(tag = "HouseApi.updateHouse") {
        ktorClient.patch("/houses/$houseId") {
            setBody(request)
        }.body()
    }

    override suspend fun getHouseById(houseId: Long): ApiResult<HouseResponse> = safeApiCall(tag = "HouseApi.getHouseById") {
        ktorClient.get("/houses/$houseId").body()
    }

    override suspend fun addMember(houseId: Long, memberId: Long): ApiResult<HouseResponse> = safeApiCall(tag = "HouseApi.addMember") {
        ktorClient.put("/houses/$houseId/members/$memberId").body()
    }

    override suspend fun removeMember(houseId: Long, memberId: Long): ApiResult<HouseResponse> = safeApiCall(tag = "HouseApi.removeMember") {
        ktorClient.delete("/houses/$houseId/members/$memberId").body()
    }
}
