package com.pollyannawu.justwoo.backend.schema

import org.jetbrains.exposed.dao.id.LongIdTable

internal object Users : LongIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
}
