package com.pollyannawu.justwoo.backend.schema

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Profiles : LongIdTable("profiles") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val name = varchar("name", 100)
    val avatar = text("avatar")
    val bankAccount = varchar("bank_account", 50)
    val createTime = timestamp("create_time")
    val updateTime = timestamp("update_time")


    fun toDomain(row: org.jetbrains.exposed.sql.ResultRow) = com.pollyannawu.justwoo.core.Profile(
        id = row[userId].value,
        name = row[name],
        avatar = row[avatar],
        bankAccount = row[bankAccount],
        createTime = row[createTime],
        updateTime = row[updateTime]
    )


    fun from(it: org.jetbrains.exposed.sql.statements.UpdateBuilder<*>, profile: com.pollyannawu.justwoo.core.Profile) {
        it[userId] = profile.id
        it[name] = profile.name
        it[avatar] = profile.avatar
        it[bankAccount] = profile.bankAccount
        it[createTime] = profile.createTime
        it[updateTime] = profile.updateTime
    }
}