package com.pollyannawu.justwoo.backend.schema

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

internal object Users : LongIdTable("users") {
    val email = varchar("email", 512).uniqueIndex()
    val password = varchar("password", 512)
    val createTime = timestamp("create_time")
    val updateTime = timestamp("update_time")
}
