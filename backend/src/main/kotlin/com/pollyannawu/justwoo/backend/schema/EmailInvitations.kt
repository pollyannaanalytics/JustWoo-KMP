package com.pollyannawu.justwoo.backend.schema

import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

data class EmailInvitationRow(
    val id: Long,
    val houseId: Long,
    val inviteeEmail: String,
    val code: String,
    val expiresAt: Instant,
    val used: Boolean,
)

internal object EmailInvitations : LongIdTable("email_invitations") {
    val houseId = reference("house_id", Houses, onDelete = ReferenceOption.CASCADE)
    val inviteeEmail = varchar("invitee_email", 512)
    val code = varchar("code", 8)
    val expiresAt = timestamp("expires_at")
    val used = bool("used").default(false)

    fun toDomain(row: ResultRow) = EmailInvitationRow(
        id = row[id].value,
        houseId = row[houseId].value,
        inviteeEmail = row[inviteeEmail],
        code = row[code],
        expiresAt = row[expiresAt],
        used = row[used],
    )
}
