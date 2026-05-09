package com.pollyannawu.justwoo.backend.schema

import com.pollyannawu.justwoo.core.Settlement
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.UpdateBuilder

internal object Settlements : LongIdTable("settlements") {
    val houseId = long("house_id")
    val payerId = long("payer_id")
    val payeeId = long("payee_id")
    val amount = double("amount")
    val currencyCode = varchar("currency_code", length = 10)
    val note = varchar("note", 500).default("")
    val createTime = timestamp("create_time")

    fun from(it: UpdateBuilder<*>, settlement: Settlement) {
        it[houseId] = settlement.houseId
        it[payerId] = settlement.payerId
        it[payeeId] = settlement.payeeId
        it[amount] = settlement.amount
        it[currencyCode] = settlement.currencyCode
        it[note] = settlement.note
        it[createTime] = settlement.createTime
    }

    fun toDomain(row: ResultRow) = Settlement(
        id = row[id].value,
        houseId = row[houseId],
        payerId = row[payerId],
        payeeId = row[payeeId],
        amount = row[amount],
        currencyCode = row[currencyCode],
        note = row[note],
        createTime = row[createTime]
    )
}
