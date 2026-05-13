package com.pollyannawu.justwoo.di

import app.cash.sqldelight.db.SqlDriver
import com.pollyannawu.justwoo.db.DriverFactory
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {
    single { DriverFactory(androidContext()) }
    single<SqlDriver> { get<DriverFactory>().create() }
    single(IO_DISPATCHER) { Dispatchers.IO }
}
