package com.pollyannawu.justwoo.data.network

import com.pollyannawu.justwoo.config.AppConfig
import com.pollyannawu.justwoo.data.datasource.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath

fun createHttpClient(
    engine: HttpClientEngine,
    config: AppConfig,
    json: Json,
    tokenStorage: TokenStorage,
    tokenRefresher: TokenRefresher,
): HttpClient = HttpClient(engine) {

    expectSuccess = true

    install(ContentNegotiation) { json(json) }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
    }

    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 2)
        retryOnException(maxRetries = 2, retryOnTimeout = true)
        exponentialDelay()
    }

    install(Logging) {
        level = if (config.isDebug) LogLevel.ALL else LogLevel.INFO
        logger = Logger.DEFAULT
    }

    install(Auth) {
        bearer {
            loadTokens {
                tokenStorage.getTokens()?.let {
                    BearerTokens(it.accessToken, it.refreshToken)
                }
            }
            refreshTokens {
                val refresh = oldTokens?.refreshToken ?: return@refreshTokens null
                val new = tokenRefresher.refresh(refresh) ?: run {
                    tokenStorage.clear()
                    return@refreshTokens null
                }
                tokenStorage.saveTokens(new)
                BearerTokens(new.accessToken, new.refreshToken)
            }

            sendWithoutRequest { req -> !req.url.encodedPath.startsWith("/auth/") }
        }
    }
    defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)
    }
}

fun createRefreshClient(
    engine: HttpClientEngine,
    config: AppConfig,
    json: Json
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
    }
    install(Logging) {
        level = if (config.isDebug) LogLevel.ALL else LogLevel.INFO
        logger = Logger.DEFAULT
    }
    defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)
    }
}