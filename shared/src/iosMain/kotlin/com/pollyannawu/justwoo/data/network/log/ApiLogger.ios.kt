package com.pollyannawu.justwoo.data.network.log

private class IosApiLogger : ApiLogger {
    override fun log(level: ApiLogLevel, tag: String, message: String, throwable: Throwable?) {
        val prefix = when (level) {
            ApiLogLevel.DEBUG -> "D"
            ApiLogLevel.INFO -> "I"
            ApiLogLevel.WARN -> "W"
            ApiLogLevel.ERROR -> "E"
        }
        println("$prefix/$tag: $message" + if (throwable != null) " | ${throwable.message}" else "")
        throwable?.printStackTrace()
    }
}

actual fun defaultApiLogger(): ApiLogger = IosApiLogger()
