package com.pollyannawu.justwoo.backend.utils.security

import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*


class CustomAuthConfig(name: String?) : AuthenticationProvider.Config(name) {
    lateinit var securityService: SecurityService
}
class CustomAuthProvider(config: CustomAuthConfig) : AuthenticationProvider(config) {
    private val securityService = config.securityService

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")

        val claims = token?.let { securityService.verifyToken(it) }

        if (claims != null) {
            val userId = claims["userId"] ?: ""
            context.principal(UserIdPrincipal(userId))
        } else {
            context.challenge("CustomAuth", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired token")
                challenge.complete()
            }
        }
    }
}

fun AuthenticationConfig.customAuth(
    name: String? = "auth-jwt",
    configure: CustomAuthConfig.() -> Unit
) {
    val config = CustomAuthConfig(name).apply(configure)
    register(CustomAuthProvider(config))
}