package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.utils.dbQuery
import com.pollyannawu.justwoo.backend.schema.Settlements
import com.pollyannawu.justwoo.backend.schema.Tasks
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.Task
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

interface SettlementRepository {
    suspend fun createSettlement(settlement: Settlement): Settlement
    suspend fun getSettlements(houseId: Long): List<Settlement>
    suspend fun getTasksWithPrice(houseId: Long): List<Task>
    suspend fun getSettlementById(houseId: Long, settlementId: Long): Settlement?
    suspend fun updateSettlement(settlement: Settlement): Settlement
}

internal class DefaultSettlementRepository : SettlementRepository {

    private val log = LoggerFactory.getLogger("SettlementRepository")

    override suspend fun createSettlement(settlement: Settlement): Settlement = dbQuery {
        log.trace("createSettlement houseId={}", settlement.houseId)
        val id = Settlements.insertAndGetId { Settlements.from(it, settlement) }.value
        Settlements.selectAll().where { Settlements.id eq id }.single().let { Settlements.toDomain(it) }
    }

    override suspend fun getSettlements(houseId: Long): List<Settlement> = dbQuery {
        Settlements.selectAll()
            .where { Settlements.houseId eq houseId }
            .orderBy(Settlements.createTime)
            .map { Settlements.toDomain(it) }
    }

    override suspend fun getTasksWithPrice(houseId: Long): List<Task> = dbQuery {
        Tasks.selectAll()
            .where { (Tasks.houseId eq houseId) and Tasks.price.isNotNull() }
            .map { Tasks.toDomain(it) }
    }

    override suspend fun getSettlementById(houseId: Long, settlementId: Long): Settlement? = dbQuery {
        Settlements.selectAll()
            .where { (Settlements.id eq settlementId) and (Settlements.houseId eq houseId) }
            .singleOrNull()?.let { Settlements.toDomain(it) }
    }

    override suspend fun updateSettlement(settlement: Settlement): Settlement = dbQuery {
        log.trace("updateSettlement id={}", settlement.id)
        Settlements.update(where = { Settlements.id eq settlement.id }) {
            it[payerId] = settlement.payerId
            it[payeeId] = settlement.payeeId
            it[amount] = settlement.amount
            it[currencyCode] = settlement.currencyCode
            it[note] = settlement.note
        }
        Settlements.selectAll().where { Settlements.id eq settlement.id }.single().let { Settlements.toDomain(it) }
    }
}
