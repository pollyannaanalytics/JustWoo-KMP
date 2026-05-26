package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.utils.dbQuery
import com.pollyannawu.justwoo.backend.schema.InviteCodeRow
import com.pollyannawu.justwoo.backend.schema.InviteCodes
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

interface InviteCodeRepository {
    suspend fun create(houseId: Long, code: String, expiresAt: Instant, createdBy: Long): InviteCodeRow
    suspend fun findByCode(code: String): InviteCodeRow?
    suspend fun markUsed(id: Long)
}

internal class DefaultInviteCodeRepository : InviteCodeRepository {

    override suspend fun create(
        houseId: Long,
        code: String,
        expiresAt: Instant,
        createdBy: Long,
    ): InviteCodeRow = dbQuery {
        val id = InviteCodes.insertAndGetId {
            it[InviteCodes.houseId] = houseId
            it[InviteCodes.code] = code
            it[InviteCodes.expiresAt] = expiresAt
            it[InviteCodes.used] = false
            it[InviteCodes.createdBy] = createdBy
            it[InviteCodes.createdAt] = Clock.System.now()
        }.value
        InviteCodes.selectAll().where { InviteCodes.id eq id }.single().let { InviteCodes.toDomain(it) }
    }

    override suspend fun findByCode(code: String): InviteCodeRow? = dbQuery {
        InviteCodes.selectAll().where { InviteCodes.code eq code }.singleOrNull()?.let { InviteCodes.toDomain(it) }
    }

    override suspend fun markUsed(id: Long): Unit = dbQuery {
        InviteCodes.update({ InviteCodes.id eq id }) { it[used] = true }
    }
}
