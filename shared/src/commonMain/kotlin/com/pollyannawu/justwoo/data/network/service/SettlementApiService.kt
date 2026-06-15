package com.pollyannawu.justwoo.data.network.service

import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import com.pollyannawu.justwoo.core.dto.UpdateSettlementRequest
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

interface SettlementApiService {
    suspend fun getSettlements(houseId: Long): ApiResult<List<SettlementResponse>>
    suspend fun createSettlement(houseId: Long, request: CreateSettlementRequest): ApiResult<SettlementResponse>
    suspend fun updateSettlement(houseId: Long, settlementId: Long, request: UpdateSettlementRequest): ApiResult<SettlementResponse>
    suspend fun getBalance(houseId: Long): ApiResult<HouseBalanceResponse>
}

class DefaultSettlementApiService(
    private val ktorClient: HttpClient,
) : SettlementApiService {

    override suspend fun getSettlements(houseId: Long): ApiResult<List<SettlementResponse>> = safeApiCall(tag = "SettlementApi.getSettlements") {
        ktorClient.get("/houses/$houseId/settlements").body()
    }

    override suspend fun createSettlement(
        houseId: Long,
        request: CreateSettlementRequest,
    ): ApiResult<SettlementResponse> = safeApiCall(tag = "SettlementApi.createSettlement") {
        ktorClient.post("/houses/$houseId/settlements") {
            setBody(request)
        }.body()
    }

    override suspend fun updateSettlement(
        houseId: Long,
        settlementId: Long,
        request: UpdateSettlementRequest,
    ): ApiResult<SettlementResponse> = safeApiCall(tag = "SettlementApi.updateSettlement") {
        ktorClient.put("/houses/$houseId/settlements/$settlementId") {
            setBody(request)
        }.body()
    }

    override suspend fun getBalance(houseId: Long): ApiResult<HouseBalanceResponse> = safeApiCall(tag = "SettlementApi.getBalance") {
        ktorClient.get("/houses/$houseId/settlements/balance").body()
    }
}
