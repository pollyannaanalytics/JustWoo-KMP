package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.service.AuthService
import com.pollyannawu.justwoo.backend.utils.dataresult.AuthDataResult
import com.pollyannawu.justwoo.core.dto.LoginRequest
import com.pollyannawu.justwoo.core.dto.RefreshRequest
import com.pollyannawu.justwoo.core.dto.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Routing.authRoute() {

    val authService by inject<AuthService>()

    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result = authService.register(
                email = request.email,
                plainPassword = request.plainPassword,
                deviceId = request.deviceId
            )
            call.respondAuthResult(result)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = authService.loginByEmailAndPassword(request.email, request.password, request.deviceId)
            call.respondAuthResult(result)
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()
            val result = authService.refresh(request.deviceId, request.token)
            call.respondAuthResult(result)
        }
    }
}




suspend fun <T> ApplicationCall.respondAuthResult(result: AuthDataResult<T>) {
    when (result) {
        is AuthDataResult.Success -> {
            if (result.data == null) respond(HttpStatusCode.NoContent)
            else respond(HttpStatusCode.OK, result.data)
        }
        is AuthDataResult.Failure -> {
            val status = when (result) {
                is AuthDataResult.Failure.PasswordError -> HttpStatusCode.Unauthorized
                is AuthDataResult.Failure.HasRegisteredUser -> HttpStatusCode.Conflict
                is AuthDataResult.Failure.OutOfLoginAttemptLimit -> HttpStatusCode.TooManyRequests
                else -> HttpStatusCode.BadRequest
            }
            respond(status, result.toString()) // 建議定義一個 ErrorResponse DTO
        }
    }
}
