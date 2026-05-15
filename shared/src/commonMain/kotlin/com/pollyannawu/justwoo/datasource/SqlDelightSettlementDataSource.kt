package com.pollyannawu.justwoo.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.db.JustWooDatabase
import com.pollyannawu.justwoo.db.SettlementEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightSettlementDataSource(
    private val db: JustWooDatabase,
    private val dispatcher: CoroutineDispatcher,
) : SettlementDataSource {

    private val queries get() = db.settlementQueries

    override fun getSettlements(): Flow<List<Settlement>> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getSettlementsByHouseId(houseId: Long): Flow<List<Settlement>> =
        queries.selectByHouseId(houseId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getSettlementById(id: Long): Settlement? = withContext(dispatcher) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun saveSettlement(settlement: Settlement): Unit = withContext(dispatcher) {
        queries.upsert(settlement.toEntity())
    }

    override suspend fun saveSettlements(settlements: List<Settlement>) = withContext(dispatcher) {
        queries.transaction {
            settlements.forEach { queries.upsert(it.toEntity()) }
        }
    }

    override suspend fun deleteSettlement(id: Long) {
        withContext(dispatcher) {
            queries.deleteById(id)
        }
    }
}

private fun Settlement.toEntity(): SettlementEntity = SettlementEntity(
    id = id,
    houseId = houseId,
    payerId = payerId,
    payeeId = payeeId,
    amount = amount,
    currencyCode = currencyCode,
    note = note,
    createTime = createTime,
)

private fun SettlementEntity.toDomain(): Settlement = Settlement(
    id = id,
    houseId = houseId,
    payerId = payerId,
    payeeId = payeeId,
    amount = amount,
    currencyCode = currencyCode,
    note = note,
    createTime = createTime,
)
