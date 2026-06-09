package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import com.pollyannawu.justwoo.data.datasource.SettlementDataSource
import com.pollyannawu.justwoo.data.network.service.SettlementApiService
import com.pollyannawu.justwoo.model.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class DefaultSettlementRepository(
    private val settlementApiService: SettlementApiService,
    private val settlementDataSource: SettlementDataSource,
) : SettlementRepository {

    override fun observeSettlements(houseId: Long): Flow<List<Settlement>> =
        settlementDataSource.getSettlementsByHouseId(houseId)

    override suspend fun syncSettlements(houseId: Long) {
        val result = settlementApiService.getSettlements(houseId)
        if (result is ApiResult.Success) {
            val settlements = result.data.map { it.toDomain() }
            settlementDataSource.saveSettlements(settlements)
        }
    }

    override suspend fun getBalance(houseId: Long): ApiResult<HouseBalanceResponse> =
        settlementApiService.getBalance(houseId)

    override suspend fun createSettlement(houseId: Long, request: CreateSettlementRequest): ApiResult<SettlementResponse> {
        val result = settlementApiService.createSettlement(houseId, request)
        if (result is ApiResult.Success) {
            settlementDataSource.saveSettlement(result.data.toDomain())
        }
        return result
    }
}

private fun com.pollyannawu.justwoo.core.dto.SettlementResponse.toDomain() = Settlement(
    id = id,
    houseId = houseId,
    payerId = payerId,
    payeeId = payeeId,
    amount = amount,
    currencyCode = currencyCode,
    note = note,
    createTime = Instant.parse(createTime),
)
