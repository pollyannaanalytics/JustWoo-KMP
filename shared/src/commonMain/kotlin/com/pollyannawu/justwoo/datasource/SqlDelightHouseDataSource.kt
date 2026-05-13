package com.pollyannawu.justwoo.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.db.HouseEntity
import com.pollyannawu.justwoo.db.HouseMemberEntity
import com.pollyannawu.justwoo.db.JustWooDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class SqlDelightHouseDataSource(
    private val db: JustWooDatabase,
    private val dispatcher: CoroutineDispatcher,
) : HouseDataSource {

    private val houseQueries get() = db.houseQueries
    private val memberQueries get() = db.houseMemberQueries

    override fun getHouses(): Flow<List<House>> =
        combine(
            houseQueries.selectAll().asFlow().mapToList(dispatcher),
            memberQueries.selectAll().asFlow().mapToList(dispatcher),
        ) { houses, members ->
            val byHouse = members.groupBy { it.houseId }
            houses.map { entity -> entity.toDomain(byHouse[entity.id].orEmpty()) }
        }

    override suspend fun getHouseById(id: Long): House = withContext(dispatcher) {
        val entity = houseQueries.selectById(id).executeAsOne()
        val members = memberQueries.selectByHouseId(id).executeAsList()
        entity.toDomain(members)
    }

    override suspend fun createHouse(house: House) = upsertOne(house)

    override suspend fun updateHouse(house: House) = upsertOne(house)

    override suspend fun updateHouses(houses: List<House>) = withContext(dispatcher) {
        houseQueries.transaction {
            houses.forEach { writeHouseInTransaction(it) }
        }
    }

    override suspend fun deleteHouse(id: Long) {
        withContext(dispatcher) {
            houseQueries.deleteById(id)
        }
    }

    private suspend fun upsertOne(house: House) = withContext(dispatcher) {
        houseQueries.transaction {
            writeHouseInTransaction(house)
        }
    }

    private fun writeHouseInTransaction(house: House) {
        houseQueries.upsert(house.toEntity())
        memberQueries.deleteByHouseId(house.id)
        house.members.forEach { m ->
            memberQueries.upsert(
                HouseMemberEntity(
                    houseId = house.id,
                    userId = m.userId,
                    role = m.role,
                    joinedAt = m.joinedAt,
                ),
            )
        }
    }
}

private fun House.toEntity(): HouseEntity = HouseEntity(
    id = id,
    name = name,
    avatar = avatar,
    description = description,
    createTime = createTime,
    updateTime = updateTime,
)

private fun HouseEntity.toDomain(members: List<HouseMemberEntity>): House = House(
    id = id,
    name = name,
    avatar = avatar,
    description = description,
    members = members.map {
        HouseMember(
            houseId = it.houseId,
            userId = it.userId,
            role = it.role,
            joinedAt = it.joinedAt,
        )
    },
    createTime = createTime,
    updateTime = updateTime,
)
