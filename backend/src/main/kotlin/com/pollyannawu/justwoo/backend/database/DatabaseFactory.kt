package com.pollyannawu.justwoo.backend.database

import com.pollyannawu.justwoo.backend.schema.Chats
import com.pollyannawu.justwoo.backend.schema.ChatsTasks
import com.pollyannawu.justwoo.backend.schema.HouseMembers
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
import org.slf4j.LoggerFactory

object DatabaseFactory {

    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    private const val DATABASE_HOST_PATH = "database.host"
    private const val DATABASE_PORT_PATH = "database.port"
    private const val DATABASE_DB_NAME_PATH = "database.dbName"
    private const val DATABASE_USER_PATH = "database.user"
    private const val DATABASE_PASSWORD_PATH = "database.password"

    fun init(config: ApplicationConfig){
        val host = config.propertyOrNull(DATABASE_HOST_PATH)?.getString() ?: "localhost"
        val port = config.propertyOrNull(DATABASE_PORT_PATH)?.getString() ?: "5432"
        val dbName = config.propertyOrNull(DATABASE_DB_NAME_PATH)?.getString() ?: "justwoo"


        val (jdbcUrl, driver) = if (host.contains("mem")) {
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" to "org.h2.Driver"
        } else {
            "jdbc:postgresql://$host:$port/$dbName" to "org.postgresql.Driver"
        }

        val user = config.propertyOrNull(DATABASE_USER_PATH)?.getString() ?: ""
        val password = config.propertyOrNull(DATABASE_PASSWORD_PATH)?.getString() ?: ""
        
        try {
            val database = Database.connect(jdbcUrl, driver, user, password)

            transaction(database) {
                SchemaUtils.create(
                    Tasks, Houses, Users, Profiles, Chats,
                    TasksAssignees, HouseMembers, ChatsTasks
                )
            }
        } catch (e: Exception) {
            logger.error("database connection failed: ${e.message}")
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}