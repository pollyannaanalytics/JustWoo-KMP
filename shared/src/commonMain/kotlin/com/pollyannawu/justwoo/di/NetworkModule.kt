package com.pollyannawu.justwoo.di

import com.pollyannawu.justwoo.config.AppConfig
import com.pollyannawu.justwoo.network.AuthApiService
import com.pollyannawu.justwoo.network.DefaultAuthApiService
import com.pollyannawu.justwoo.network.DefaultHouseApiService
import com.pollyannawu.justwoo.network.DefaultProfileApiService
import com.pollyannawu.justwoo.network.DefaultSettlementApiService
import com.pollyannawu.justwoo.network.DefaultTaskApiService
import com.pollyannawu.justwoo.network.DefaultTokenRefresher
import com.pollyannawu.justwoo.datasource.DefaultTokenStorage
import com.pollyannawu.justwoo.network.HouseApiService
import com.pollyannawu.justwoo.network.ProfileApiService
import com.pollyannawu.justwoo.network.SettlementApiService
import com.pollyannawu.justwoo.network.TaskApiService
import com.pollyannawu.justwoo.network.TokenRefresher
import com.pollyannawu.justwoo.datasource.TokenStorage
import com.pollyannawu.justwoo.network.createHttpClient
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val BASE_URL = "https://api.justwoo-tw.uk"

@OptIn(ExperimentalSettingsApi::class)
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

    // FlowSettings provided by platformModule (EncryptedSharedPreferences / KeychainSettings)
    single<TokenStorage> { DefaultTokenStorage(get()) }

    // AuthDataSource provided by androidModule / iosModule (needed for deviceId)
    single<TokenRefresher> { DefaultTokenRefresher(get(), get()) }

    single {
        createHttpClient(
            engine = get(),       // provided by platformModule
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
