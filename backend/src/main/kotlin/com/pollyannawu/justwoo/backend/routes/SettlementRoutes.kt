package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.service.SettlementService
import com.pollyannawu.justwoo.backend.utils.dataresult.SettlementDataResult
import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.UpdateSettlementRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.settlementRoute() {
    val settlementService by inject<SettlementService>()

    authenticate("auth-jwt") {
        val getUserId = { call: ApplicationCall ->
            call.principal<UserIdPrincipal>()?.name?.toLongOrNull()
        }

        route("houses/{houseId}/settlements") {

            // GET /houses/{houseId}/settlements — 查看所有沖銷紀錄
            get {
                val houseId = call.parameters["houseId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ERROR_MSG_HOUSE_ID_MISSING)
                val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)

                call.respondSettlementResult(settlementService.getSettlements(houseId, userId))
            }

            // POST /houses/{houseId}/settlements — 新增沖銷/付款紀錄
            post {
                val houseId = call.parameters["houseId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ERROR_MSG_HOUSE_ID_MISSING)
                val userId = getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<CreateSettlementRequest>()
                call.respondSettlementResult(settlementService.createSettlement(houseId, userId, request))
            }

            // PUT /houses/{houseId}/settlements/{settlementId} — 編輯既有沖銷/付款紀錄
            put("{settlementId}") {
                val houseId = call.parameters["houseId"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ERROR_MSG_HOUSE_ID_MISSING)
                val settlementId = call.parameters["settlementId"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "settlementId is missing or invalid")
                val userId = getUserId(call) ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<UpdateSettlementRequest>()
                call.respondSettlementResult(settlementService.updateSettlement(houseId, settlementId, userId, request))
            }

            // GET /houses/{houseId}/settlements/balance — 查看結算餘額（per-currency, no conversion）
            get("balance") {
                val houseId = call.parameters["houseId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ERROR_MSG_HOUSE_ID_MISSING)
                val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)

                call.respondSettlementResult(settlementService.getHouseBalance(houseId, userId))
            }
        }
    }
}

private suspend inline fun <reified T : Any> ApplicationCall.respondSettlementResult(result: SettlementDataResult<T>) {
    when (result) {
        is SettlementDataResult.Success -> respond(HttpStatusCode.OK, result.data)
        is SettlementDataResult.Error -> {
            val (status, message) = when (result) {
                is SettlementDataResult.Error.DatabaseError -> HttpStatusCode.InternalServerError to result.message
                is SettlementDataResult.Error.UserNotAllowed -> HttpStatusCode.Forbidden to "User ${result.id} is not a member"
                is SettlementDataResult.Error.HouseNotFound -> HttpStatusCode.NotFound to "House not found"
                is SettlementDataResult.Error.InvalidAmount -> HttpStatusCode.BadRequest to "Amount must be greater than zero"
                is SettlementDataResult.Error.InvalidCurrency -> HttpStatusCode.BadRequest to "Unknown or unsupported currency code: ${result.code}"
                is SettlementDataResult.Error.SettlementNotFound -> HttpStatusCode.NotFound to "Settlement not found"
                is SettlementDataResult.Error.Forbidden -> HttpStatusCode.Forbidden to "User ${result.id} is not allowed to edit this settlement"
                is SettlementDataResult.Error.SelfPayment -> HttpStatusCode.BadRequest to "payerId and payeeId must be different"
            }
            respond(status, mapOf("error" to message))
        }
    }
}
