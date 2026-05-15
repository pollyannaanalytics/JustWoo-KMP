package com.pollyannawu.justwoo.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.db.JustWooDatabase
import com.pollyannawu.justwoo.db.ProfileEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightProfileDataSource(
    private val db: JustWooDatabase,
    private val dispatcher: CoroutineDispatcher,
) : ProfileDataSource {

    private val queries get() = db.profileQueries

    override fun getProfiles(): Flow<List<Profile>> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeProfileById(id: Long): Flow<Profile?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }

    override suspend fun getProfileById(id: Long): Profile? = withContext(dispatcher) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun saveProfile(profile: Profile): Unit = withContext(dispatcher) {
        queries.upsert(profile.toEntity())
    }

    override suspend fun saveProfiles(profiles: List<Profile>) = withContext(dispatcher) {
        queries.transaction {
            profiles.forEach { queries.upsert(it.toEntity()) }
        }
    }

    override suspend fun deleteProfile(id: Long) {
        withContext(dispatcher) {
            queries.deleteById(id)
        }
    }
}

private fun Profile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    avatar = avatar,
    bankAccount = bankAccount,
    createTime = createTime,
    updateTime = updateTime,
)

private fun ProfileEntity.toDomain(): Profile = Profile(
    id = id,
    name = name,
    avatar = avatar,
    bankAccount = bankAccount,
    createTime = createTime,
    updateTime = updateTime,
)
