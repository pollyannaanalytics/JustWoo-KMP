package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import kotlinx.coroutines.flow.Flow

interface SettlementRepository {
    fun observeSettlements(houseId: Long): Flow<List<Settlement>>
    suspend fun syncSettlements(houseId: Long)
    suspend fun getBalance(houseId: Long): Result<HouseBalanceResponse>
    suspend fun createSettlement(houseId: Long, request: CreateSettlementRequest): Result<SettlementResponse>
}
