package com.pollyannawu.justwoo.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.util.logging.Logger
import io.ktor.utils.io.core.Closeable

import io.ktor.serialization.kotlinx.json.json

class ApiClient(
    private val baseUrl: String,
    private val appLogger: Logger,
): Closeable {
    companion object {
        private const val LOG_TAG = "APIClient"
    }

    var userId: String? = null

    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
}