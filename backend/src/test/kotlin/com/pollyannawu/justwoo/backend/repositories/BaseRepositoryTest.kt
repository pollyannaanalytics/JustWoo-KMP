package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.schema.Chats
import com.pollyannawu.justwoo.backend.schema.ChatsTasks
import com.pollyannawu.justwoo.backend.schema.HouseMembers
import com.pollyannawu.justwoo.backend.schema.Houses
import com.pollyannawu.justwoo.backend.schema.Profiles
import com.pollyannawu.justwoo.backend.schema.Settlements
import com.pollyannawu.justwoo.backend.schema.Tasks
import com.pollyannawu.justwoo.backend.schema.TasksAssignees
import com.pollyannawu.justwoo.backend.schema.Users
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

abstract class BaseRepositoryTest {

    companion object {
        private var database: Database? = null

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            if (database == null) {
                database = Database.connect(
                    url = "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                    driver = "org.h2.Driver"
                )
            }
        }

        fun now(): Instant = Clock.System.now()

        fun insertUser(email: String, password: String = "hashed_password"): Long = transaction {
            val now = now()
            Users.insertAndGetId {
                it[Users.email] = email
                it[Users.password] = password
                it[Users.createTime] = now
                it[Users.updateTime] = now
            }.value
        }
    }

    @BeforeEach
    fun resetSchema() {
        transaction {
            SchemaUtils.drop(
                ChatsTasks, TasksAssignees, HouseMembers,
                Chats, Tasks, Settlements, Profiles, Houses, Users
            )
            SchemaUtils.create(
                Users, Houses, Profiles, Tasks, Chats,
                HouseMembers, TasksAssignees, ChatsTasks, Settlements
            )
        }
    }
}
