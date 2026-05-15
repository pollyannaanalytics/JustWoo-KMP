package com.pollyannawu.justwoo.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.cash.sqldelight.db.SqlDriver
import com.pollyannawu.justwoo.db.DriverFactory
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val FILE_SECURE_PREF = "justwoo_secure_pref"
val platformModule = module {
    single { DriverFactory(androidContext()) }
    single<SqlDriver> { get<DriverFactory>().create() }
    single(IO_DISPATCHER) { Dispatchers.IO }
    single<HttpClientEngine> { OkHttp.create() }

    single<Settings>(SECURE_SETTINGS) {
        val context = androidContext()
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            FILE_SECURE_PREF,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        SharedPreferencesSettings(encryptedPrefs)
    }
}
