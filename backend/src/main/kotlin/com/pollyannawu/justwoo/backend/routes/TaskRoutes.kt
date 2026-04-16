package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.service.TaskService
import com.pollyannawu.justwoo.backend.utils.dataresult.TaskDataResult
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
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


fun Route.taskRoute() {
    val taskService by inject<TaskService>()
    authenticate("auth-jwt") {
        val getUserId = { call: ApplicationCall ->
            call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
        }

        route("houses/{houseId}") {
            route("/tasks") {
                get {
                    val houseId = call.parameters["houseId"]?.toLong()
                    val userId = getUserId(call)

                    if (houseId != null && userId != null) {
                        val newTask = taskService.getTaskDetails(houseId, userId)
                        call.respondResult(newTask)
                    }
                }

                post {
                    val houseId = call.parameters["houseId"]?.toLongOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid House ID")

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

                get("/owner/{ownerId}") {
                    val houseId =
                        call.parameters["houseId"]?.toLongOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid House ID"
                        )
                    val ownerId =
                        call.parameters["ownerId"]?.toLongOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid Owner ID"
                        )
                    val userId =
                        getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val result = taskService.getTasksByOwnerId(houseId, userId, ownerId)
                    call.respondResult(result)
                }

                get("/assignee/{assigneeId}") {
                    val houseId =
                        call.parameters["houseId"]?.toLongOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid House ID"
                        )
                    val assigneeId =
                        call.parameters["assigneeId"]?.toLongOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid Assignee ID"
                        )
                    val userId =
                        getUserId(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val result = taskService.getTasksByAssigneeId(houseId, userId, assigneeId)
                    call.respondResult(result)
                }

            }
        }

        route("/tasks/{taskId}") {
            patch("/content") {
                val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid House ID"
                )
                val userId = getUserId(call) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val task = call.receive<Task>()

                val result = taskService.updateTaskContent(houseId, userId, task)
                call.respondResult(result)
            }

            patch("/assign-status") {
                val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid House ID"
                )
                val taskId = call.parameters["taskId"]?.toLongOrNull() ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val assignee = call.receive<TaskAssignee>()

                val result = taskService.updateTaskAssignStatus(houseId, taskId, assignee)
                call.respondResult(result)
            }

            // PATCH /houses/{houseId}/tasks/{taskId}/status
            patch("/status") {
                val houseId = call.parameters["houseId"]?.toLongOrNull() ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid House ID"
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