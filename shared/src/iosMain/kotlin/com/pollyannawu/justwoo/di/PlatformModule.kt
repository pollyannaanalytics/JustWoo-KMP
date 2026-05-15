package com.pollyannawu.justwoo.di

import app.cash.sqldelight.db.SqlDriver
import com.pollyannawu.justwoo.db.DriverFactory
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module


@OptIn(ExperimentalSettingsImplementation::class)
val platformModule = module {
    single { DriverFactory() }
    single<SqlDriver> { get<DriverFactory>().create() }
    single(IO_DISPATCHER) { Dispatchers.Default }
    single<HttpClientEngine> { Darwin.create() }
    single<Settings>(SECURE_SETTINGS){
        KeychainSettings(service = "com.pollyannawu.justwoo")
    }
}
