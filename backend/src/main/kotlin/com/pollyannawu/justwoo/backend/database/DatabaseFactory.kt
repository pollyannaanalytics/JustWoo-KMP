package com.pollyannawu.justwoo.backend.database

import com.pollyannawu.justwoo.backend.schema.Chats
import com.pollyannawu.justwoo.backend.schema.ChatsTasks
import com.pollyannawu.justwoo.backend.schema.HouseMembers
import com.pollyannawu.justwoo.backend.schema.HouseRules
import com.pollyannawu.justwoo.backend.schema.Houses
import com.pollyannawu.justwoo.backend.schema.Profiles
import com.pollyannawu.justwoo.backend.schema.Tasks
import com.pollyannawu.justwoo.backend.schema.TasksAssignees
import com.pollyannawu.justwoo.backend.schema.Users
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(config: ApplicationConfig){
        val driverName = "org.postgresql.Driver"
        val jdbcUrl = config.property("storage.jdbcUrl").getString()
        val user = config.property("storage.user").getString()
        val password = config.property("storage.password").getString()
        val database = Database.connect(jdbcUrl, driverName, user, password)

        transaction(database) {
            SchemaUtils.create(
                Tasks, Houses, Users, Profiles, Chats,
                TasksAssignees, HouseMembers, ChatsTasks, HouseRules
            )
        }

    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}