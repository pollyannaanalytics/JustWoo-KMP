package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.DatabaseFactory.dbQuery
import com.pollyannawu.justwoo.backend.schema.Profiles
import com.pollyannawu.justwoo.core.Profile
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

interface ProfileRepository {
    suspend fun getProfileById(userId: Long): Profile
    suspend fun getProfiles(userIds: List<Long>): List<Profile>
    suspend fun createProfile(profile: Profile): Profile
    suspend fun updateProfile(profile: Profile): Profile
}

internal class DefaultProfileRepository : ProfileRepository {
    private val log = LoggerFactory.getLogger("ProfileRepository")

    private fun getProfile(userId: Long): Profile {
        log.trace("start getProfile for userId: {}", userId)
        val row = Profiles.selectAll()
            .where { Profiles.userId eq userId }
            .singleOrNull() ?: throw NoSuchElementException("Profile not found for userId: $userId")

        return Profiles.toDomain(row)
    }

    override suspend fun getProfileById(userId: Long): Profile = dbQuery {
       getProfile(userId)
    }

    override suspend fun getProfiles(userIds: List<Long>): List<Profile> = dbQuery {
        log.trace("start getProfiles for userIds: {}", userIds)
        Profiles.selectAll()
            .where { Profiles.userId inList userIds }
            .map { Profiles.toDomain(it) }
    }

    override suspend fun createProfile(profile: Profile): Profile = dbQuery {
        log.trace("start createProfile")
        Profiles.insertAndGetId {
            Profiles.from(it, profile)
        }
        getProfile(profile.id)
    }

    override suspend fun updateProfile(profile: Profile): Profile = dbQuery {
        log.trace("start updateProfile for userId: {}", profile.id)
        Profiles.update({ Profiles.userId eq profile.id }) {
            Profiles.from(it, profile)
        }
        getProfile(profile.id)
    }
}
