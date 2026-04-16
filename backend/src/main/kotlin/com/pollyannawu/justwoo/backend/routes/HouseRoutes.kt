package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.service.HouseService
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseDataResult
import com.pollyannawu.justwoo.core.dto.HouseRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.houseRoute() {
    val houseService by inject<HouseService>()
    authenticate("auth-jwt") {
        val getUserId = { call: ApplicationCall ->
            call.principal<UserIdPrincipal>()?.name?.toLongOrNull()
        }

        route("/houses") {
            get {
                val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val result = houseService.getHouses(userId)
                call.respondResult(result)
            }

            post {
                getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                try {
                    val request = call.receive<HouseRequest>()
                    val result = houseService.createHouse(request)
                    call.respondResult(result)
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON format $e")
                }
            }

            route("/{houseId}") {
                get {
                    val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val houseId = call.parameters["houseId"]?.toLongOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid House ID")
                    val result = houseService.getHouse(userId, houseId)
                    call.respondResult(result)
                }

                patch {
                    val userId = getUserId(call) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                    val houseId = call.parameters["houseId"]?.toLongOrNull()
                        ?: return@patch call.respond(HttpStatusCode.BadRequest, "Invalid House ID")
                    try {
                        val request = call.receive<HouseRequest>()
                        val result = houseService.updateHouseContent(userId, houseId, request)
                        call.respondResult(result)
                    } catch (e: ContentTransformationException) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid JSON format $e")
                    }
                }

                route("/members") {
                    post("/{memberId}") {
                        val requesterId = getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                        val houseId = call.parameters["houseId"]?.toLongOrNull()
                            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid House ID")
                        val memberId = call.parameters["memberId"]?.toLongOrNull()
                            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid Member ID")

                        val result = houseService.addMember(requesterId, memberId, houseId)
                        call.respondResult(result)
                    }

                    delete("/{memberId}") {
                        val requesterId = getUserId(call) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                        val houseId = call.parameters["houseId"]?.toLongOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid House ID")
                        val memberId = call.parameters["memberId"]?.toLongOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid Member ID")

                        val result = houseService.removeMember(requesterId, memberId, houseId)
                        call.respondResult(result)
                    }
                }
            }
        }
    }
}

private suspend fun <T> ApplicationCall.respondResult(result: HouseDataResult<T>) {
    when (result) {
        is HouseDataResult.Success -> respond(HttpStatusCode.OK, result.data as Any)
        is HouseDataResult.Error -> {
            val (status, message) = when (result) {
                is HouseDataResult.Error.DatabaseError -> HttpStatusCode.InternalServerError to (result.message)
                is HouseDataResult.Error.UserNotAllowed -> HttpStatusCode.Forbidden to "User ${result.id} is not allowed (${result.type})"
                is HouseDataResult.Error.HouseNotFound, HouseDataResult.Error.NotFound -> HttpStatusCode.NotFound to "House not found"
            }
            respond(status, mapOf("error" to message))
        }
    }
}
