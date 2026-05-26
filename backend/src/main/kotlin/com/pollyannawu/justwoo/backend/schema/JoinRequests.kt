package com.pollyannawu.justwoo.backend.schema

import com.pollyannawu.justwoo.core.JoinRequestStatus
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

data class JoinRequestRow(
    val id: Long,
    val houseId: Long,
    val userId: Long,
    val inviteCodeId: Long,
    val status: JoinRequestStatus,
    val createdAt: Instant,
)

internal object JoinRequests : LongIdTable("join_requests") {
    val houseId = reference("house_id", Houses, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val inviteCodeId = reference("invite_code_id", InviteCodes, onDelete = ReferenceOption.CASCADE)
    val status = integer("status").default(JoinRequestStatus.PENDING.value)
    val createdAt = timestamp("created_at")

    fun toDomain(row: ResultRow) = JoinRequestRow(
        id = row[id].value,
        houseId = row[houseId].value,
        userId = row[userId].value,
        inviteCodeId = row[inviteCodeId].value,
        status = JoinRequestStatus.entries.first { it.value == row[status] },
        createdAt = row[createdAt],
    )
}
