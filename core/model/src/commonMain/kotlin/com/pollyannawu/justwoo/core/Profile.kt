package com.pollyannawu.justwoo.core

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: Long,
    val name: String,
    val avatar: String,
    val bankAccount: String,
    val bio: String,
    val hashtags: List<String>,
    val updateTime: Instant,
    val createTime: Instant,
)
