package com.pollyannawu.justwoo.backend.utils.security

import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import org.koin.java.KoinJavaComponent.getKoin


class JustwooAuthProvider(config: Config) : AuthenticationProvider(config) {

    class Config(name: String?) : AuthenticationProvider.Config(name) {
        lateinit var tokenProvider: AccessTokenProvider
    }

    private val tokenProvider = (config as Config).tokenProvider

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        println("Raw Header Token: ${call.request.headers["Authorization"]}")
        val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
        val decodedJWT= token?.let { tokenProvider.verifyAccessToken(it) }

        println("Auth Debug: Token = $token, Claims = $decodedJWT")
        val userId = decodedJWT?.getClaim("userId")?.asLong()

        if (userId != null) {
            println("User Id: $userId")
            context.principal(UserIdPrincipal(userId.toString()))
        } else {
            context.challenge("CustomAuth", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
                call.respond(HttpStatusCode.Unauthorized, "Debug: Auth Failed, userId was $userId")
                challenge.complete()
            }
        }
    }
}

fun AuthenticationConfig.customAuth(name: String? = "auth-jwt") {
    val tokenProvider = getKoin().get<AccessTokenProvider>()
    val config = JustwooAuthProvider.Config(name).apply {
        this.tokenProvider = tokenProvider
    }
    register(JustwooAuthProvider(config))
}