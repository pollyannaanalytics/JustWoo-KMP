package com.pollyannawu.justwoo.backend.schema

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

internal object Houses : LongIdTable("houses") {
    val name = varchar("name", 255)
    val description = text("description")
    val avatar = text("avatar")
}

internal object HouseMembers : LongIdTable("house_members") {
    val houseId = reference("house_id", Houses, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
}

internal object HouseRules : LongIdTable("house_rules") {
    val houseId = reference("house_id", Houses, onDelete = ReferenceOption.CASCADE)
    val rule = text("rule")
}