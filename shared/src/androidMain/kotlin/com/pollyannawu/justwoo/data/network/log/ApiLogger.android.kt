package com.pollyannawu.justwoo.data.network.log

import android.util.Log

private class AndroidApiLogger : ApiLogger {
    override fun log(level: ApiLogLevel, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            ApiLogLevel.DEBUG -> Log.d(tag, message, throwable)
            ApiLogLevel.INFO -> Log.i(tag, message, throwable)
            ApiLogLevel.WARN -> Log.w(tag, message, throwable)
            ApiLogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }
}

actual fun defaultApiLogger(): ApiLogger = AndroidApiLogger()
