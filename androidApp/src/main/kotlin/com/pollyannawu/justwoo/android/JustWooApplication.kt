package com.pollyannawu.justwoo.android

import android.app.Application
import com.pollyannawu.justwoo.android.di.androidModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class JustWooApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@JustWooApplication)
            modules(androidModule)
        }
    }
}
