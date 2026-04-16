package com.pollyannawu.justwoo.backend.utils.mapper

import com.pollyannawu.justwoo.core.User
import com.pollyannawu.justwoo.core.dto.UserResponse
import kotlinx.datetime.Clock


val now = Clock.System.now()


fun User.toResponse(): UserResponse = UserResponse(
    id = this.id,
    email = this.email,
    createTime = now
)
