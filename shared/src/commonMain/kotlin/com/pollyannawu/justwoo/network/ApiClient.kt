package com.pollyannawu.justwoo.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.takeFrom
import io.ktor.utils.io.core.Closeable
import io.ktor.client.plugins.logging.Logger as KtorLogger

import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException

class ApiClient(
    private val apiUrl: String,
    private val appLogger: Logger,
): Closeable {
    companion object {
        private const val LOG_TAG = "APIClient"
        private const val BASE_URL = "https://api.justwoo-tw.uk"
    }

    var userId: String? = null

    private val client = HttpClient {
        install(ContentNegotiation) { json() }
        defaultRequest {
            url(BASE_URL)
        }


        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : KtorLogger {
                override fun log(message: String) {
                    appLogger.log(LOG_TAG)
                }
            }
        }

        expectSuccess = true
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }

        install(DefaultRequest) {
            url.takeFrom(apiUrl)
        }
    }

    override fun close() {
       client.close()
    }

    private suspend fun <T> safeApiCall(
        call: suspend () -> T,
    ): T? {
        return try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            appLogger.log(LOG_TAG + ": " + e.message + ": " + e.cause)
            null
        }
    }
}