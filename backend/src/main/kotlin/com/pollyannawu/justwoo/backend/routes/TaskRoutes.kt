package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.service.TaskService
import com.pollyannawu.justwoo.backend.utils.dataresult.TaskDataResult
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
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


const val ERROR_MSG_HOUSE_ID_MISSING = "House Id is missing"
fun Route.taskRoute() {
    val taskService by inject<TaskService>()
    authenticate("auth-jwt") {
        val getUserId = { call: ApplicationCall ->
            call.principal<UserIdPrincipal>()?.name?.toLongOrNull()
        }

        route("houses/{houseId}/tasks") {
            get {
                val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ERROR_MSG_HOUSE_ID_MISSING
                )
                val userId = getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val ownerId = call.request.queryParameters["ownerId"]?.toLongOrNull() ?: 0L
                val assigneeId = call.request.queryParameters["assigneeId"]?.toLongOrNull() ?: 0L

                val result = when {
                    ownerId != 0L -> taskService.getTasksByOwnerId(houseId, userId, ownerId)
                    assigneeId != 0L -> taskService.getTasksByAssigneeId(houseId, userId, assigneeId)
                    else -> taskService.getTaskDetails(houseId, userId)
                }

                call.respondResult(result)

            }

            post {
                val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ERROR_MSG_HOUSE_ID_MISSING
                )

                val userId =
                    getUserId(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                try {
                    val request = call.receive<CreateTaskRequest>()
                    val result = taskService.createTask(houseId, userId, request)
                    call.respondResult(result)
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
                }
            }

            route("{taskId}") {
                patch {
                    val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ERROR_MSG_HOUSE_ID_MISSING
                    )
                    val userId = getUserId(call) ?: return@patch call.respond(HttpStatusCode.Unauthorized)

                    val task = call.receive<Task>()
                    call.respondResult(taskService.updateTaskContent(houseId, userId, task))
                }

                route("assignees/{userId}") {
                    patch {
                        val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@patch call.respond(
                            HttpStatusCode.BadRequest,
                            ERROR_MSG_HOUSE_ID_MISSING
                        )
                        val taskId = call.parameters["taskId"]?.toLongOrNull() ?: return@patch call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid Task ID"
                        )
                        val userId = call.parameters["userId"]?.toLongOrNull() ?: return@patch call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid User ID"
                        )
                        val status = call.receive<AssignStatus>()
                        val assignee = TaskAssignee(userId = userId, status = status)

                        val result = taskService.updateTaskAssignStatus(houseId, taskId, assignee)
                        call.respondResult(result)
                    }
                }

                patch("status") {
                    val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ERROR_MSG_HOUSE_ID_MISSING
                    )
                    val taskId = call.parameters["taskId"]?.toLongOrNull() ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid Task ID"
                    )
                    val userId = getUserId(call) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                    val newStatus = call.receive<TaskStatus>()

                    val result = taskService.updateTaskStatus(houseId, userId, taskId, newStatus)
                    call.respondResult(result)
                }
            }
        }
    }
}

private suspend fun <T> ApplicationCall.respondResult(result: TaskDataResult<T>) {
    when (result) {
        is TaskDataResult.Success -> respond(HttpStatusCode.OK, result.data as Any)
        is TaskDataResult.Error -> {
            val (status, message) = when (result) {
                is TaskDataResult.Error.DatabaseError -> HttpStatusCode.InternalServerError to result.message
                is TaskDataResult.Error.UserNotAllowed -> HttpStatusCode.Forbidden to "User ${result.id} is not a member"
                is TaskDataResult.Error.NotFound, TaskDataResult.Error.HouseNotFound -> HttpStatusCode.NotFound to "Resource not found"
                else -> HttpStatusCode.BadRequest to "Unknown error"
            }
            respond(status, mapOf("error" to message))
        }
    }
}