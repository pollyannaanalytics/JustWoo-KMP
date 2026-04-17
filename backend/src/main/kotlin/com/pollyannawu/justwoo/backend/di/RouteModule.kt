package com.pollyannawu.justwoo.backend.di

import com.pollyannawu.justwoo.backend.routes.authRoute
import com.pollyannawu.justwoo.backend.routes.houseRoute
import com.pollyannawu.justwoo.backend.routes.profileRoute
import com.pollyannawu.justwoo.backend.routes.taskRoute
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.routeModule(){
    routing {
        authRoute()
        taskRoute()
        houseRoute()
        profileRoute()
        get("/hello") {
            call.respondText("你好，JustWoo 後端已就緒！")
        }
    }
}