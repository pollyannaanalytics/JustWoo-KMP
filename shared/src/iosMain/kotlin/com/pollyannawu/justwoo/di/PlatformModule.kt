package com.pollyannawu.justwoo.di

import app.cash.sqldelight.db.SqlDriver
import com.pollyannawu.justwoo.data.db.DriverFactory
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults


@OptIn(ExperimentalSettingsImplementation::class)
val platformModule = module {
    single { DriverFactory() }
    single<SqlDriver> { get<DriverFactory>().create() }
    single(IO_DISPATCHER) { Dispatchers.Default }
    single<HttpClientEngine> { Darwin.create() }

    single<Settings>(SECURE_SETTINGS) {
        KeychainSettings(service = "com.pollyannawu.justwoo")
    }

    single<ObservableSettings>(PREFS_SETTINGS) {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}
