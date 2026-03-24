//package com.pollyannawu.justwoo.backend.repositories
//
//import com.pollyannawu.justwoo.backend.schema.Chats
//import com.pollyannawu.justwoo.backend.schema.ChatsTasks
//import com.pollyannawu.justwoo.backend.schema.HouseMembers
//import com.pollyannawu.justwoo.backend.schema.Houses
//import com.pollyannawu.justwoo.backend.schema.Profiles
//import com.pollyannawu.justwoo.backend.schema.Tasks
//import com.pollyannawu.justwoo.backend.schema.TasksAssignees
//import com.pollyannawu.justwoo.backend.schema.Users
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.SchemaUtils
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.BeforeEach
//
//abstract class BaseRepositoryTest {
//
//    companion object {
//        private var database: Database? = null
//
//        @JvmStatic
//        @BeforeAll
//        fun setupDatabase() {
//            if (database == null) {
//                // DB_CLOSE_DELAY=-1 keeps the in-memory database alive as long as the JVM is running
//                database = Database.connect(
//                    url = "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
//                    driver = "org.h2.Driver"
//                )
//            }
//        }
//    }
//
//    @BeforeEach
//    fun setupSchema() {
//        transaction {
//            // Drop tables in correct order
//            SchemaUtils.drop(
//                ChatsTasks, TasksAssignees, HouseMembers,
//                Chats, Tasks, Profiles, Houses, Users
//            )
//            // Create tables
//            SchemaUtils.create(
//                Users, Houses, Profiles, Tasks, Chats,
//                HouseMembers, TasksAssignees, ChatsTasks
//            )
//        }
//    }
//}
