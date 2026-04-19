package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.utils.PagedResult
import com.pollyannawu.justwoo.backend.database.utils.dbQuery
import com.pollyannawu.justwoo.backend.database.utils.toPagedRows
import com.pollyannawu.justwoo.backend.schema.HouseMembers
import com.pollyannawu.justwoo.backend.schema.Houses
import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import kotlin.Int

interface HouseRepository {
    suspend fun isMember(userId: Long, houseId: Long): Boolean
    suspend fun getPagedHouses(userId: Long, houseId: Long? = null, size: Int,
                               offset: Long,): PagedResult<House>
    suspend fun createHouse(house: House, userId: Long): House
    suspend fun addMember(userId: Long, memberRole: MemberRole, houseId: Long, joinedAt: Instant): House
    suspend fun removeMember(userId: Long, houseId: Long): House
    suspend fun updateHouseContent(house: House): House
    suspend fun getHouseMembers(houseId: Long): List<HouseMember>
}

internal class DefaultHouseRepository: HouseRepository{
    private val log = LoggerFactory.getLogger("HouseRepository")

    override suspend fun isMember(userId: Long, houseId: Long): Boolean = dbQuery {
        log.trace("start check isMember")
        val resultRow = HouseMembers.selectAll().where{
            (HouseMembers.houseId eq houseId) and (HouseMembers.memberId eq userId)
        }.toList()
        log.trace("members: {}", resultRow.size)
        return@dbQuery resultRow.isNotEmpty()
    }

    override suspend fun getPagedHouses(
        userId: Long,
        houseId: Long?,
        size: Int,
        offset: Long,
    ): PagedResult<House> = dbQuery{
        log.trace("start getHouseDetails")
        val idQuery = (Houses innerJoin HouseMembers)
            .select(Houses.id)
            .where { HouseMembers.memberId eq userId }

        if (houseId != null) idQuery.andWhere { Houses.id eq houseId }

        val pagedIdResult = idQuery
            .orderBy(Houses.createTime to SortOrder.DESC)
            .toPagedRows(size, offset)

        if (pagedIdResult.items.isEmpty()) {
            return@dbQuery PagedResult(emptyList(), pagedIdResult.totalCount)
        }

        val targetIds = pagedIdResult.items.map { it[Houses.id].value }

        val detailedRows = (Houses leftJoin HouseMembers).selectAll().where{
            Houses.id inList targetIds
        }.orderBy(Houses.createTime to SortOrder.DESC)
            .toList()

        val houses = detailedRows.toHouseDomain()

        PagedResult(houses, pagedIdResult.totalCount)
    }


    override suspend fun createHouse(house: House, userId: Long): House = dbQuery {
        log.trace("start createHouse")
        val houseId = Houses.insertAndGetId {
            Houses.from(it, house)
        }.value

        log.trace("house id: {}", houseId)

        val membersToAdd = if (house.members.any { it.userId == userId }) {
            house.members
        } else {
            house.members + HouseMember(userId = userId, role = MemberRole.ADMIN, houseId = houseId, joinedAt = Clock.System.now())
        }

        HouseMembers.batchInsert(membersToAdd) { member ->
            this[HouseMembers.houseId] = houseId
            this[HouseMembers.memberId] = member.userId
            this[HouseMembers.joinAt] = member.joinedAt
            this[HouseMembers.role] = member.role.value
        }

       return@dbQuery getHouseById(houseId) ?: throw IllegalStateException("House was not created successfully")
    }

    override suspend fun addMember(
        userId: Long,
        memberRole: MemberRole,
        houseId: Long,
        joinedAt: Instant
    ): House = dbQuery {
        log.trace("start addMember")
        HouseMembers.insert{

            HouseMembers.from(it, houseId, userId, memberRole, joinedAt)
        }

       return@dbQuery getHouseById(houseId) ?: throw IllegalStateException("House was not created successfully")
    }

    override suspend fun removeMember(
        userId: Long,
        houseId: Long
    ): House = dbQuery {
        log.trace("start removeMember")
        HouseMembers.deleteWhere { (memberId eq userId) and (HouseMembers.houseId eq houseId) }
        return@dbQuery getHouseById(houseId) ?: throw IllegalStateException("House not found after member removal")
    }

    override suspend fun updateHouseContent(house: House): House = dbQuery {
        log.trace("start updateHouseContent")
        Houses.update({ Houses.id eq house.id }) {
            Houses.from(it, house)
        }
        return@dbQuery getHouseById(house.id) ?: throw IllegalStateException("House not found after update")
    }

    override suspend fun getHouseMembers(houseId: Long): List<HouseMember> = dbQuery {
        log.trace("start getHouseMembers")
        val resultRow = HouseMembers.selectAll().where { HouseMembers.houseId eq houseId }.toList()

        log.trace("Rows found: ${resultRow.size}")
        val members = resultRow.map {
            HouseMembers.toDomain(it)
        }
        return@dbQuery members
    }

    private fun getHouseById(houseId: Long): House? {
        val houseExists = Houses.selectAll().where { Houses.id eq houseId }.any()
        log.trace("House exists check: {}, ID: {}", houseExists, houseId)

        val rows = (Houses leftJoin HouseMembers)
            .selectAll()
            .where { Houses.id eq houseId }
            .toList()

        log.trace("Rows size after join: {}", rows.size)

        if (rows.isEmpty()) return null

        return rows.toHouseDomain().firstOrNull()
    }

    private fun List<ResultRow>.toHouseDomain(): List<House> {
        return this.groupBy { it[Houses.id] }.map { (_, rows) ->
            val members = rows.mapNotNull { row ->
                if (row.getOrNull(HouseMembers.id) != null) {
                    HouseMembers.toDomain(row)
                } else null
            }
            Houses.toDomain(rows.first(), members)
        }
    }
}
