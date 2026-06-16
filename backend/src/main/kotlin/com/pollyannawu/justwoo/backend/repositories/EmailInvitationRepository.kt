package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.utils.dbQuery
import com.pollyannawu.justwoo.backend.schema.EmailInvitationRow
import com.pollyannawu.justwoo.backend.schema.EmailInvitations
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

interface EmailInvitationRepository {
    suspend fun create(houseId: Long, email: String, code: String, expiresAt: Instant): EmailInvitationRow
    suspend fun invalidateExisting(houseId: Long, email: String)
    suspend fun findActiveByHouseAndEmail(houseId: Long, email: String): EmailInvitationRow?
}

internal class DefaultEmailInvitationRepository : EmailInvitationRepository {

    override suspend fun create(
        houseId: Long,
        email: String,
        code: String,
        expiresAt: Instant,
    ): EmailInvitationRow = dbQuery {
        val id = EmailInvitations.insertAndGetId {
            it[EmailInvitations.houseId] = houseId
            it[EmailInvitations.inviteeEmail] = email
            it[EmailInvitations.code] = code
            it[EmailInvitations.expiresAt] = expiresAt
            it[EmailInvitations.used] = false
        }.value
        EmailInvitations.selectAll().where { EmailInvitations.id eq id }
            .single().let { EmailInvitations.toDomain(it) }
    }

    override suspend fun invalidateExisting(houseId: Long, email: String): Unit = dbQuery {
        val now = Clock.System.now()
        EmailInvitations.update({
            (EmailInvitations.houseId eq houseId) and
            (EmailInvitations.inviteeEmail eq email) and
            (EmailInvitations.used eq false) and
            (EmailInvitations.expiresAt greater now)
        }) {
            it[used] = true
        }
    }

    override suspend fun findActiveByHouseAndEmail(houseId: Long, email: String): EmailInvitationRow? = dbQuery {
        val now = Clock.System.now()
        EmailInvitations.selectAll().where {
            (EmailInvitations.houseId eq houseId) and
            (EmailInvitations.inviteeEmail eq email) and
            (EmailInvitations.used eq false) and
            (EmailInvitations.expiresAt greater now)
        }.singleOrNull()?.let { EmailInvitations.toDomain(it) }
    }
}
