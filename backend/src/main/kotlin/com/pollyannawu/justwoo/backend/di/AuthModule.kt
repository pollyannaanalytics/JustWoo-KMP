package com.pollyannawu.justwoo.backend.di

import com.pollyannawu.justwoo.backend.repositories.auth.LoginAttemptRepository
import com.pollyannawu.justwoo.backend.repositories.auth.RedisLoginAttemptRepository
import com.pollyannawu.justwoo.backend.repositories.auth.RedisRefreshTokenRepository
import com.pollyannawu.justwoo.backend.repositories.auth.RefreshTokenRepository
import com.pollyannawu.justwoo.backend.utils.security.AccessTokenProvider
import com.pollyannawu.justwoo.backend.utils.security.JwtAccessTokenProvider
import org.koin.dsl.module


val authModule = module {
    single<AccessTokenProvider>{ JwtAccessTokenProvider(get()) }
    single<RefreshTokenRepository>{ RedisRefreshTokenRepository(get())}
    single<LoginAttemptRepository> { RedisLoginAttemptRepository(get()) }
}