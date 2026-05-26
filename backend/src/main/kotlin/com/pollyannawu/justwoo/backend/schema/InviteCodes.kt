package com.pollyannawu.justwoo.backend.schema

import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

data class InviteCodeRow(
    val id: Long,
    val houseId: Long,
    val code: String,
    val expiresAt: Instant,
    val used: Boolean,
    val createdBy: Long,
    val createdAt: Instant,
)

internal object InviteCodes : LongIdTable("invite_codes") {
    val houseId = reference("house_id", Houses, onDelete = ReferenceOption.CASCADE)
    val code = varchar("code", 10).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val used = bool("used").default(false)
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at")

    fun toDomain(row: ResultRow) = InviteCodeRow(
        id = row[id].value,
        houseId = row[houseId].value,
        code = row[code],
        expiresAt = row[expiresAt],
        used = row[used],
        createdBy = row[createdBy].value,
        createdAt = row[createdAt],
    )
}
