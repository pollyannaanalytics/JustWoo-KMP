package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import com.pollyannawu.justwoo.core.dto.UpdateSettlementRequest
import com.pollyannawu.justwoo.data.datasource.SettlementDataSource
import com.pollyannawu.justwoo.data.network.service.SettlementApiService
import com.pollyannawu.justwoo.model.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant

class DefaultSettlementRepository(
    private val settlementApiService: SettlementApiService,
    private val settlementDataSource: SettlementDataSource,
) : SettlementRepository {

    override fun observeSettlements(houseId: Long): Flow<List<Settlement>> =
        settlementDataSource.getSettlementsByHouseId(houseId)
            .onStart { syncSettlements(houseId) }

    override suspend fun syncSettlements(houseId: Long) {
        val result = settlementApiService.getSettlements(houseId)
        if (result is ApiResult.Success) {
            settlementDataSource.saveSettlements(result.data.map { it.toDomain() })
        }
    }

    override suspend fun getBalance(houseId: Long): Result<HouseBalanceResponse> =
        settlementApiService.getBalance(houseId).toResult()

    override suspend fun createSettlement(houseId: Long, request: CreateSettlementRequest): Result<SettlementResponse> {
        val apiResult = settlementApiService.createSettlement(houseId, request)
        if (apiResult is ApiResult.Success) {
            settlementDataSource.saveSettlement(apiResult.data.toDomain())
        }
        return apiResult.toResult()
    }

    override suspend fun updateSettlement(houseId: Long, settlementId: Long, request: UpdateSettlementRequest): Result<SettlementResponse> {
        val apiResult = settlementApiService.updateSettlement(houseId, settlementId, request)
        if (apiResult is ApiResult.Success) {
            settlementDataSource.saveSettlement(apiResult.data.toDomain())
        }
        return apiResult.toResult()
    }

    override suspend fun getSettlementById(id: Long): Settlement? =
        settlementDataSource.getSettlementById(id)
}

private fun <T> ApiResult<T>.toResult(): Result<T> = when (this) {
    is ApiResult.Success -> Result.success(data)
    is ApiResult.Error -> Result.failure(exception)
    ApiResult.Loading -> Result.failure(IllegalStateException("Unexpected loading state"))
}

private fun SettlementResponse.toDomain() = Settlement(
    id = id,
    houseId = houseId,
    payerId = payerId,
    payeeId = payeeId,
    amount = amount,
    currencyCode = currencyCode,
    note = note,
    createTime = Instant.parse(createTime),
)
