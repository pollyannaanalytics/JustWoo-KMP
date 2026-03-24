package com.pollyannawu.justwoo.backend

import com.pollyannawu.justwoo.backend.database.DatabaseFactory
import com.pollyannawu.justwoo.backend.di.repositoryModule
import com.pollyannawu.justwoo.backend.di.routeModule
import com.pollyannawu.justwoo.backend.di.servicesModule
import com.pollyannawu.justwoo.backend.utils.security.SecurityService
import com.pollyannawu.justwoo.backend.utils.security.customAuth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun Application.diModule(){

    install(Koin){
        modules(
            servicesModule,
            repositoryModule,
            module{
                single<CoroutineScope> { this@diModule }
                single { createClient() } onClose { it?.close() }
                single { environment.config }
            }
        )
    }
    val securityService by inject<SecurityService>()

    install(Authentication){
        customAuth("auth-jwt") {
            this.securityService = securityService
        }
    }

    DatabaseFactory.init(environment.config)

    routeModule()

    monitor.subscribe(ApplicationStopped) {
        it.getKoin().close()
    }
}

private fun createClient() = HttpClient {
    install(ContentNegotiation) {
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