package com.pollyannawu.justwoo.backend.schema

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Profiles : LongIdTable("profiles") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val name = varchar("name", 100)
    val avatar = text("avatar")
    val bankAccount = varchar("bank_account", 50)
}