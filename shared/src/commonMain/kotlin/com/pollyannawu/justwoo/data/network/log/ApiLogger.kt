package com.pollyannawu.justwoo.data.network.log

/** Severity of a log line, mapped onto each platform's native logger. */
enum class ApiLogLevel { DEBUG, INFO, WARN, ERROR }

/** KMP-friendly logging façade for network calls. */
interface ApiLogger {
    fun log(level: ApiLogLevel, tag: String, message: String, throwable: Throwable? = null)
}

fun ApiLogger.d(tag: String, message: String) = log(ApiLogLevel.DEBUG, tag, message)
fun ApiLogger.i(tag: String, message: String) = log(ApiLogLevel.INFO, tag, message)
fun ApiLogger.w(tag: String, message: String, t: Throwable? = null) = log(ApiLogLevel.WARN, tag, message, t)
fun ApiLogger.e(tag: String, message: String, t: Throwable? = null) = log(ApiLogLevel.ERROR, tag, message, t)

/** Platform-provided default. Android uses `android.util.Log`; iOS uses `println`. */
expect fun defaultApiLogger(): ApiLogger
