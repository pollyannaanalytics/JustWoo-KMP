package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.service.ProfileService
import com.pollyannawu.justwoo.backend.utils.dataresult.ProfileDataResult
import com.pollyannawu.justwoo.core.dto.ProfileRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.profileRoute() {
    val profileService by inject<ProfileService>()

    authenticate("auth-jwt") {
        val getUserId = { call: ApplicationCall ->
            call.principal<UserIdPrincipal>()?.name?.toLongOrNull()
        }

        route("/profiles") {
            post {
                val userId = getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<ProfileRequest>()
                val result = profileService.createProfile(userId, request)
                call.respondProfileResult(result)
            }

            get("/me") {
                val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val result = profileService.getProfileById(userId)
                call.respondProfileResult(result)
            }

            patch("/me") {
                val userId = getUserId(call) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                try {
                    val request = call.receive<ProfileRequest>()
                    val result = profileService.updateProfile(userId, request)
                    call.respondProfileResult(result)
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
                }
            }

            get("/{userId}") {
                val userId = call.parameters["userId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid User ID")
                val result = profileService.getProfileById(userId)
                call.respondProfileResult(result)
            }
        }
    }
}

private suspend fun <T> ApplicationCall.respondProfileResult(result: ProfileDataResult<T>) {
    when (result) {
        is ProfileDataResult.Success -> respond(HttpStatusCode.OK, result.data as Any)
        is ProfileDataResult.Error -> {
            val (status, message) = when (result) {
                is ProfileDataResult.Error.NotFound -> HttpStatusCode.NotFound to "Profile not found"
                is ProfileDataResult.Error.DatabaseError -> HttpStatusCode.InternalServerError to result.message
            }
            respond(status, mapOf("error" to message))
        }
    }
}
