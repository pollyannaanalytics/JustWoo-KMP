package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.utils.dataresult.ProfileDataResult
import com.pollyannawu.justwoo.backend.utils.mapper.toResponse
import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.core.dto.ProfileRequest
import com.pollyannawu.justwoo.core.dto.ProfileResponse
import kotlinx.datetime.Clock

interface ProfileService {
    suspend fun createProfile(userId: Long, request: ProfileRequest): ProfileDataResult<ProfileResponse>
    suspend fun updateProfile(userId: Long, request: ProfileRequest): ProfileDataResult<ProfileResponse>
    suspend fun getProfileById(userId: Long): ProfileDataResult<ProfileResponse>
    suspend fun getProfilesByIds(ids: List<Long>): ProfileDataResult<List<ProfileResponse>>
}

class DefaultProfileService(
    private val profileRepository: ProfileRepository
) : ProfileService {

    override suspend fun createProfile(userId: Long,request: ProfileRequest): ProfileDataResult<ProfileResponse> {
        val now = Clock.System.now()
        val profile = Profile(
            id = userId,
            name = request.name,
            avatar = request.avatar,
            bankAccount = request.bankAccount,
            createTime = now,
            updateTime = now
        )
        val created = profileRepository.createProfile(profile)
        return ProfileDataResult.Success(created.toResponse())
    }

    override suspend fun updateProfile(userId: Long, request: ProfileRequest): ProfileDataResult<ProfileResponse> {
        return try {
            val existing = profileRepository.getProfileById(userId)
            val updated = profileRepository.updateProfile(
                existing.copy(
                    name = request.name,
                    avatar = request.avatar,
                    bankAccount = request.bankAccount,
                    updateTime = Clock.System.now()
                )
            )
            ProfileDataResult.Success(updated.toResponse())
        } catch (e: NoSuchElementException) {
            ProfileDataResult.Error.NotFound
        }
    }

    override suspend fun getProfileById(userId: Long): ProfileDataResult<ProfileResponse> {
        return try {
            val profile = profileRepository.getProfileById(userId)
            ProfileDataResult.Success(profile.toResponse())
        } catch (e: NoSuchElementException) {
            ProfileDataResult.Error.NotFound
        }
    }

    override suspend fun getProfilesByIds(ids: List<Long>): ProfileDataResult<List<ProfileResponse>> {
        val profiles = profileRepository.getProfiles(ids)
        return ProfileDataResult.Success(profiles.map { it.toResponse() })
    }
}
