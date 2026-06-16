package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.service.HouseInviteService
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseDataResult
import com.pollyannawu.justwoo.core.dto.EmailInvitationRequest
import com.pollyannawu.justwoo.core.dto.JoinRequestBody
import com.pollyannawu.justwoo.core.dto.JoinRequestDecision
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

fun Route.inviteRoute() {
    val inviteService by inject<HouseInviteService>()

    authenticate("auth-jwt") {
        val getUserId = { call: ApplicationCall ->
            call.principal<UserIdPrincipal>()?.name?.toLongOrNull()
        }

        route("/houses/{houseId}/invite-codes") {
            post {
                val userId = getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val houseId = call.parameters["houseId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid House ID")
                val result = inviteService.generateInviteCode(userId, houseId)
                call.respondInviteResult(result)
            }
        }

        route("/houses/{houseId}/invitations") {
            post {
                val userId = getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val houseId = call.parameters["houseId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid House ID")
                try {
                    val body = call.receive<EmailInvitationRequest>()
                    if (body.email.isBlank()) {
                        return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email is required"))
                    }
                    val result = inviteService.createEmailInvitation(userId, houseId, body.email)
                    call.respondInviteResult(result)
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                }
            }
        }

        route("/houses/{houseId}/join-requests") {
            get {
                val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val houseId = call.parameters["houseId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid House ID")
                val result = inviteService.getPendingRequests(userId, houseId)
                call.respondInviteResult(result)
            }
        }

        route("/join-requests") {
            post {
                val userId = getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                try {
                    val body = call.receive<JoinRequestBody>()
                    val result = inviteService.submitJoinRequest(userId, body.inviteCode)
                    call.respondInviteResult(result)
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                }
            }

            get("/me") {
                val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val result = inviteService.getMyJoinRequestStatus(userId)
                call.respondInviteResult(result)
            }

            patch("/{requestId}") {
                val userId = getUserId(call) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val requestId = call.parameters["requestId"]?.toLongOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Invalid request ID")
                try {
                    val decision = call.receive<JoinRequestDecision>()
                    val result = inviteService.processJoinRequest(userId, requestId, decision.approve)
                    call.respondInviteResult(result)
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                }
            }
        }
    }
}

private suspend inline fun <reified T : Any> ApplicationCall.respondInviteResult(result: HouseDataResult<T>) {
    when (result) {
        is HouseDataResult.Success -> respond(HttpStatusCode.OK, result.data)
        is HouseDataResult.Error -> {
            val (status, message) = when (result) {
                is HouseDataResult.Error.DatabaseError -> HttpStatusCode.InternalServerError to result.message
                is HouseDataResult.Error.BadRequest -> HttpStatusCode.BadRequest to result.message
                is HouseDataResult.Error.UserNotAllowed -> HttpStatusCode.Forbidden to "Forbidden"
                is HouseDataResult.Error.HouseNotFound,
                HouseDataResult.Error.NotFound -> HttpStatusCode.NotFound to "Not found"
                HouseDataResult.Error.AlreadyMember -> HttpStatusCode.Conflict to "User already belongs to a house"
                HouseDataResult.Error.InvalidCode -> HttpStatusCode.BadRequest to "Code is invalid or expired. Ask your admin to generate a new one."
                HouseDataResult.Error.AlreadyProcessed -> HttpStatusCode.Conflict to "Join request has already been processed"
            }
            respond(status, mapOf("error" to message))
        }
    }
}
