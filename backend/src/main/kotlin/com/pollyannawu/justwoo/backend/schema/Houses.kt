package com.pollyannawu.justwoo.backend.schema

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.Task
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder

internal object Houses : LongIdTable("houses") {
    val name = varchar("name", 255)
    val description = text("description")
    val avatar = text("avatar")

    val createTime = datetime("create_time")
    val updateTime = datetime("update_time")


    fun toDomain(row: ResultRow, members: List<HouseMember> = emptyList()) = House(
        id = row[id].value,
        name = row[name],
        description = row[description],
        avatar = row[avatar],
        members = members,
        createTime = row[createTime],
        updateTime = row[updateTime]
    )

    fun from(it: UpdateBuilder<*>, house: House) {
        it[name] = house.name
        it[description] = house.description
        it[avatar] = house.avatar
        it[createTime] = house.createTime
        it[updateTime] = house.updateTime
    }
}

internal object HouseMembers : LongIdTable("house_members") {
    val houseId = reference("house_id", Houses, onDelete = ReferenceOption.CASCADE)
    val memberId = reference("member_id", Users, onDelete = ReferenceOption.CASCADE)

    val role = customEnumeration(
        name = "role",
        sql = "INTEGER",
        fromDb = { value -> MemberRole.entries[value as Int] },
        toDb = { it.ordinal }
    )

    val joinAt = datetime("joined_at")

    fun from(it: UpdateBuilder<*>, houseId: Long, userId: Long, role: MemberRole) {
        it[this.houseId] = houseId
        it[this.memberId] = userId
        it[this.role] = role
    }

    fun toDomain(row: ResultRow) = HouseMember(
        houseId = row[houseId].value,
        userId = row[memberId].value,
        role = row[role],
        joinedAt = row[joinAt]
    )
}