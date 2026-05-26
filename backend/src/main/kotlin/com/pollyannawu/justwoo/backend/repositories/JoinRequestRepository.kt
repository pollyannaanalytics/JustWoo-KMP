package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.utils.dbQuery
import com.pollyannawu.justwoo.backend.schema.JoinRequestRow
import com.pollyannawu.justwoo.backend.schema.JoinRequests
import com.pollyannawu.justwoo.core.JoinRequestStatus
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

interface JoinRequestRepository {
    suspend fun create(houseId: Long, userId: Long, inviteCodeId: Long): JoinRequestRow
    suspend fun findPending(houseId: Long): List<JoinRequestRow>
    suspend fun findById(id: Long): JoinRequestRow?
    suspend fun findLatestByUser(userId: Long): JoinRequestRow?
    suspend fun updateStatus(id: Long, status: JoinRequestStatus): Boolean
    suspend fun hasActiveRequest(userId: Long): Boolean
}

internal class DefaultJoinRequestRepository : JoinRequestRepository {

    override suspend fun create(houseId: Long, userId: Long, inviteCodeId: Long): JoinRequestRow = dbQuery {
        val id = JoinRequests.insertAndGetId {
            it[JoinRequests.houseId] = houseId
            it[JoinRequests.userId] = userId
            it[JoinRequests.inviteCodeId] = inviteCodeId
            it[JoinRequests.status] = JoinRequestStatus.PENDING.value
            it[JoinRequests.createdAt] = Clock.System.now()
        }.value
        JoinRequests.selectAll().where { JoinRequests.id eq id }.single().let { JoinRequests.toDomain(it) }
    }

    override suspend fun findPending(houseId: Long): List<JoinRequestRow> = dbQuery {
        JoinRequests.selectAll().where {
            (JoinRequests.houseId eq houseId) and (JoinRequests.status eq JoinRequestStatus.PENDING.value)
        }.map { JoinRequests.toDomain(it) }
    }

    override suspend fun findById(id: Long): JoinRequestRow? = dbQuery {
        JoinRequests.selectAll().where { JoinRequests.id eq id }.singleOrNull()?.let { JoinRequests.toDomain(it) }
    }

    override suspend fun findLatestByUser(userId: Long): JoinRequestRow? = dbQuery {
        JoinRequests.selectAll()
            .where { JoinRequests.userId eq userId }
            .orderBy(JoinRequests.createdAt to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.let { JoinRequests.toDomain(it) }
    }

    override suspend fun updateStatus(id: Long, status: JoinRequestStatus): Boolean = dbQuery {
        val updated = JoinRequests.update({
            (JoinRequests.id eq id) and (JoinRequests.status eq JoinRequestStatus.PENDING.value)
        }) {
            it[JoinRequests.status] = status.value
        }
        updated > 0
    }

    override suspend fun hasActiveRequest(userId: Long): Boolean = dbQuery {
        JoinRequests.selectAll().where {
            (JoinRequests.userId eq userId) and (JoinRequests.status eq JoinRequestStatus.PENDING.value)
        }.any()
    }
}
