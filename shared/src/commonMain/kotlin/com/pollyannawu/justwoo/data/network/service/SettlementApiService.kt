package com.pollyannawu.justwoo.data.network.service

import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface SettlementApiService {
    suspend fun getSettlements(houseId: Long): ApiResult<List<SettlementResponse>>
    suspend fun createSettlement(houseId: Long, request: CreateSettlementRequest): ApiResult<SettlementResponse>
    suspend fun getBalance(houseId: Long, currency: String = "TWD"): ApiResult<HouseBalanceResponse>
}

class DefaultSettlementApiService(
    private val client: HttpClient,
) : SettlementApiService {

    override suspend fun getSettlements(houseId: Long): ApiResult<List<SettlementResponse>> = safeApiCall {
        client.get("/houses/$houseId/settlements").body()
    }

    override suspend fun createSettlement(
        houseId: Long,
        request: CreateSettlementRequest,
    ): ApiResult<SettlementResponse> = safeApiCall {
        client.post("/houses/$houseId/settlements") {
            setBody(request)
        }.body()
    }

    override suspend fun getBalance(houseId: Long, currency: String): ApiResult<HouseBalanceResponse> = safeApiCall {
        client.get("/houses/$houseId/settlements/balance") {
            parameter("currency", currency)
        }.body()
    }
}
