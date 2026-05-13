package com.pollyannawu.justwoo.datasource

import com.pollyannawu.justwoo.core.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileDataSource {
    fun getProfiles(): Flow<List<Profile>>
    fun observeProfileById(id: Long): Flow<Profile?>
    suspend fun getProfileById(id: Long): Profile?
    suspend fun saveProfile(profile: Profile)
    suspend fun saveProfiles(profiles: List<Profile>)
    suspend fun deleteProfile(id: Long)
}
