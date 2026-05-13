package com.pollyannawu.justwoo.datasource

import com.pollyannawu.justwoo.core.Settlement
import kotlinx.coroutines.flow.Flow

interface SettlementDataSource {
    fun getSettlements(): Flow<List<Settlement>>
    fun getSettlementsByHouseId(houseId: Long): Flow<List<Settlement>>
    suspend fun getSettlementById(id: Long): Settlement?
    suspend fun saveSettlement(settlement: Settlement)
    suspend fun saveSettlements(settlements: List<Settlement>)
    suspend fun deleteSettlement(id: Long)
}
