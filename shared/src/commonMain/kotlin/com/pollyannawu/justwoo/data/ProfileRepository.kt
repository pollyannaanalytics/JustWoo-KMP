package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.core.dto.ProfileRequest
import com.pollyannawu.justwoo.core.dto.ProfileResponse
import com.pollyannawu.justwoo.data.datasource.ProfileDataSource
import com.pollyannawu.justwoo.data.datasource.auth.UserStorage
import com.pollyannawu.justwoo.data.network.service.ProfileApiService
import com.pollyannawu.justwoo.model.ApiResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

interface ProfileRepository {
    fun observeCurrentProfile(): Flow<Profile?>
    suspend fun syncCurrentProfile()
    suspend fun updateCurrentProfile(name: String, bio: String, bankAccount: String): Boolean
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultProfileRepository(
    private val userStorage: UserStorage,
    private val profileApiService: ProfileApiService,
    private val profileDataSource: ProfileDataSource,
) : ProfileRepository {

    override fun observeCurrentProfile(): Flow<Profile?> =
        userStorage.userFlow.flatMapLatest { user ->
            if (user != null) profileDataSource.observeProfileById(user.id)
            else flow { emit(null) }
        }

    override suspend fun syncCurrentProfile() {
        val result = profileApiService.getMyProfile()
        if (result is ApiResult.Success) {
            profileDataSource.saveProfile(result.data.toDomain())
        }
    }

    override suspend fun updateCurrentProfile(
        name: String,
        bio: String,
        bankAccount: String,
    ): Boolean {
        val userId = userStorage.getUser()?.id ?: return false

        val current = profileDataSource.getProfileById(userId)
            ?: when (val fetched = profileApiService.getMyProfile()) {
                is ApiResult.Success -> fetched.data.toDomain().also { profileDataSource.saveProfile(it) }
                else -> return false
            }

        val request = ProfileRequest(
            name = name,
            avatar = current.avatar,
            bankAccount = bankAccount,
            bio = bio,
            hashtags = current.hashtags,
        )

        return when (val result = profileApiService.updateMyProfile(request)) {
            is ApiResult.Success -> {
                profileDataSource.saveProfile(result.data.toDomain())
                true
            }
            else -> false
        }
    }
}

private fun ProfileResponse.toDomain() = Profile(
    id = id,
    name = name,
    avatar = avatar,
    bankAccount = bankAccount,
    bio = bio,
    hashtags = hashtags,
    updateTime = updateTime,
    createTime = createTime,
)
