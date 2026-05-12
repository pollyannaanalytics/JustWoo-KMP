package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.core.dto.ProfileRequest
import com.pollyannawu.justwoo.core.dto.ProfileResponse
import com.pollyannawu.justwoo.model.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface ProfileApiService {
    suspend fun createProfile(request: ProfileRequest): ApiResult<ProfileResponse>
    suspend fun getMyProfile(): ApiResult<ProfileResponse>
    suspend fun updateMyProfile(request: ProfileRequest): ApiResult<ProfileResponse>
    suspend fun getProfileById(userId: Long): ApiResult<ProfileResponse>
}

class DefaultProfileApiService(
    private val client: HttpClient,
) : ProfileApiService {

    override suspend fun createProfile(request: ProfileRequest): ApiResult<ProfileResponse> = safeApiCall {
        client.post("/profiles") {
            setBody(request)
        }.body()
    }

    override suspend fun getMyProfile(): ApiResult<ProfileResponse> = safeApiCall {
        client.get("/profiles/me").body()
    }

    override suspend fun updateMyProfile(request: ProfileRequest): ApiResult<ProfileResponse> = safeApiCall {
        client.patch("/profiles/me") {
            setBody(request)
        }.body()
    }

    override suspend fun getProfileById(userId: Long): ApiResult<ProfileResponse> = safeApiCall {
        client.get("/profiles/$userId").body()
    }
}
