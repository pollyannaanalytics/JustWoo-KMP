package com.pollyannawu.justwoo.backend

import com.pollyannawu.justwoo.backend.database.DatabaseFactory
import com.pollyannawu.justwoo.backend.database.RedisFactory
import com.pollyannawu.justwoo.backend.di.authModule
import com.pollyannawu.justwoo.backend.di.repositoryModule
import com.pollyannawu.justwoo.backend.di.routeModule
import com.pollyannawu.justwoo.backend.di.servicesModule
import com.auth0.jwt.algorithms.Algorithm
import com.pollyannawu.justwoo.backend.utils.security.JwtConfig
import com.pollyannawu.justwoo.backend.utils.security.customAuth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin

fun Application.diModule(){

    DatabaseFactory.init(environment.config)
    RedisFactory.init(environment.config)

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    install(ContentNegotiation) {
        json()
    }

    install(Koin){
        modules(
            module{
                single<CoroutineScope> { this@diModule }
                single { getHttpClient() } onClose { it?.close() }
                single { environment.config }
                single { RedisFactory.getPool() } onClose { it?.close() }
                single {
                    val config = environment.config
                    JwtConfig(
                        issuer = config.property("jwt.issuer").getString(),
                        audience = config.property("jwt.audience").getString(),
                        algorithm = Algorithm.HMAC256(config.property("jwt.secret").getString())
                    )
                }
            },
            servicesModule,
            repositoryModule,
            authModule
        )
    }

    install(Authentication) {
        customAuth("auth-jwt")
    }

    routeModule()

    monitor.subscribe(ApplicationStopped) {
        it.getKoin().close()
    }
}

private fun getHttpClient() = HttpClient {
    install(ClientContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = false
            useArrayPolymorphism = false
        })
    }
}