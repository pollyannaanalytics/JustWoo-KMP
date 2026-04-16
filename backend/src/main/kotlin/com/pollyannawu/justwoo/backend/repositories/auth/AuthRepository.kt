package com.pollyannawu.justwoo.backend.repositories.auth

import com.pollyannawu.justwoo.backend.database.DatabaseFactory.dbQuery
import com.pollyannawu.justwoo.backend.schema.Users
import com.pollyannawu.justwoo.core.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.datetime.Clock

interface AuthRepository {
    suspend fun create(email: String, password: String): User
    suspend fun update(userId: String, email: String? = null, password: String? = null): User
    suspend fun delete(userId: String): Boolean
    suspend fun getUser(userId: String): User
    suspend fun findUserByAccount(email: String): User?
    suspend fun isValid(user: User): Boolean
}

class DefaultAuthRepository : AuthRepository {


    override suspend fun create(email: String, password: String): User = dbQuery {
        val insertedId = Users.insertAndGetId {
            it[Users.email] = email
            it[Users.password] = password
            it[Users.createTime] = Clock.System.now()
            it[Users.updateTime] = Clock.System.now()
        }
        val now = Clock.System.now()

        User(
            id = insertedId.value,
            email = email,
            passwordHash = password,
            userRefreshTokenId = 0L,
            createTime = now,
            updateTime = now,
        )
    }

    override suspend fun update(userId: String, email: String?, password: String?): User = dbQuery {
        Users.update({ Users.id eq userId.toLong() }) {
            if (email != null) it[Users.email] = email
            if (password != null) it[Users.password] = password
        }

        getUser(userId)
    }

    override suspend fun delete(userId: String): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq userId.toLong() } > 0
    }

    override suspend fun getUser(userId: String): User = dbQuery {
        Users.selectAll().where { Users.id eq userId.toLong() }
            .map { rowToUser(it) }
            .single()
    }

    override suspend fun findUserByAccount(
        email: String
    ): User? = dbQuery {
        Users.selectAll().where { Users.email eq email }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun isValid(user: User): Boolean = dbQuery {
        Users.selectAll()
            .where { (Users.id eq user.id) and (Users.password eq user.passwordHash) }
            .count() > 0
    }




    private fun rowToUser(row: ResultRow): User {
        val now = Clock.System.now()
        return User(
            id = row[Users.id].value,
            email = row[Users.email],
            passwordHash = row[Users.password],
            userRefreshTokenId = 0L,
            createTime = now,
            updateTime = now,
        )
    }

}
