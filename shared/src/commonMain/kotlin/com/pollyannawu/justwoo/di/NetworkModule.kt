package com.pollyannawu.justwoo.di

import com.pollyannawu.justwoo.config.AppConfig
import com.pollyannawu.justwoo.network.service.AuthApiService
import com.pollyannawu.justwoo.network.service.DefaultAuthApiService
import com.pollyannawu.justwoo.network.service.DefaultHouseApiService
import com.pollyannawu.justwoo.network.service.DefaultProfileApiService
import com.pollyannawu.justwoo.network.service.DefaultSettlementApiService
import com.pollyannawu.justwoo.network.service.DefaultTaskApiService
import com.pollyannawu.justwoo.datasource.auth.DefaultDeviceIdProvider
import com.pollyannawu.justwoo.datasource.auth.DefaultTokenStorage
import com.pollyannawu.justwoo.datasource.auth.DefaultUserStorage
import com.pollyannawu.justwoo.datasource.auth.DeviceIdProvider
import com.pollyannawu.justwoo.datasource.auth.TokenStorage
import com.pollyannawu.justwoo.datasource.auth.UserStorage
import com.pollyannawu.justwoo.network.DefaultTokenRefresher
import com.pollyannawu.justwoo.network.TokenRefresher
import com.pollyannawu.justwoo.network.service.HouseApiService
import com.pollyannawu.justwoo.network.service.ProfileApiService
import com.pollyannawu.justwoo.network.service.SettlementApiService
import com.pollyannawu.justwoo.network.service.TaskApiService
import com.pollyannawu.justwoo.network.createHttpClient
import com.pollyannawu.justwoo.network.createRefreshClient
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val BASE_URL = "https://api.justwoo-tw.uk"
val SECURE_SETTINGS = named("secure")
val PREFS_SETTINGS  = named("prefs")

val REFRESH_CLIENT = named("refresh")

val networkModule = module {

    single {
        AppConfig(
            baseUrl = BASE_URL,
            isDebug = false,
        )
    }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single<HttpClient>(REFRESH_CLIENT) {
        createRefreshClient(engine = get(), config = get(), json = get())
    }


    single<TokenStorage> { DefaultTokenStorage(get(SECURE_SETTINGS)) }
    single<UserStorage> { DefaultUserStorage(get(PREFS_SETTINGS), get()) }
    single<DeviceIdProvider> { DefaultDeviceIdProvider(get(SECURE_SETTINGS)) }

    single<TokenRefresher> {
        DefaultTokenRefresher(
            client = get(REFRESH_CLIENT),
            deviceIdProvider = get(),
        )
    }

    single {
        createHttpClient(
            engine = get(),
            config = get(),
            json = get(),
            tokenStorage = get(),
            tokenRefresher = get(),
        )
    }

    single<AuthApiService>       { DefaultAuthApiService(get()) }
    single<HouseApiService>      { DefaultHouseApiService(get()) }
    single<TaskApiService>       { DefaultTaskApiService(get()) }
    single<ProfileApiService>    { DefaultProfileApiService(get()) }
    single<SettlementApiService> { DefaultSettlementApiService(get()) }
}
