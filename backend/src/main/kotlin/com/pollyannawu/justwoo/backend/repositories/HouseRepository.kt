package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.DatabaseFactory.dbQuery
import com.pollyannawu.justwoo.backend.schema.HouseMembers
import com.pollyannawu.justwoo.backend.schema.Houses
import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

interface HouseRepository {
    suspend fun isMember(userId: Long, houseId: Long): Boolean
    suspend fun getHouseDetails(userId: Long, houseId: Long? = null): List<House>
    suspend fun createHouse(house: House, userId: Long): House
    suspend fun addMember(userId: Long, memberRole: MemberRole, houseId: Long): House
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
        log.trace("members: {}", resultRow)
        return@dbQuery resultRow.isNotEmpty()
    }

    override suspend fun getHouseDetails(
        userId: Long,
        houseId: Long?
    ): List<House> = dbQuery {
        log.trace("start getHouseDetails")
        val query = (Houses leftJoin HouseMembers)
            .selectAll()
        
        if (houseId != null) {
            query.where { Houses.id eq houseId }.first()
        } else {
            val userHouseIds = HouseMembers.selectAll()
                .where { HouseMembers.memberId eq userId }
                .map { it[HouseMembers.houseId].value }
            
            if (userHouseIds.isEmpty()) return@dbQuery emptyList()
            
            query.where { Houses.id inList userHouseIds }
        }

        val rows = query.toList()
        log.trace("house rows size: {}", rows.size)
        return@dbQuery rows.toHouseDomain()
    }

    override suspend fun createHouse(house: House, userId: Long): House = dbQuery {
        log.trace("start createHouse")
        val houseId = Houses.insertAndGetId {
            Houses.from(it, house)
        }.value

        if (house.members.isNotEmpty()) {
            HouseMembers.batchInsert(house.members) { member ->
                this[HouseMembers.houseId] = houseId
                this[HouseMembers.memberId] = member.userId
                this[HouseMembers.joinAt] = member.joinedAt
                this[HouseMembers.role] = member.role
            }
        }

       return@dbQuery getHouseById(houseId) ?: throw IllegalStateException("House was not created successfully")
    }

    override suspend fun addMember(
        userId: Long,
        memberRole: MemberRole,
        houseId: Long
    ): House = dbQuery {
        log.trace("start addMember")
        HouseMembers.insert{
            HouseMembers.from(it, houseId, userId, memberRole)
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

        val members = resultRow.map {
            HouseMembers.toDomain(it)
        }
        return@dbQuery members
    }

    private suspend fun getHouseById(houseId: Long): House? = dbQuery {
        val rows = (Houses leftJoin HouseMembers)
            .selectAll().where { Houses.id eq houseId }
            .toList()
        
        if (rows.isEmpty()) return@dbQuery null
        
        return@dbQuery rows.toHouseDomain().firstOrNull()
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
