package com.pollyannawu.justwoo.core

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Settlement(
    val id: Long = 0L,
    val houseId: Long,
    val payerId: Long,
    val payeeId: Long,
    val amount: Double,
    // ISO 4217 currency code, e.g. "TWD", "USD"
    val currencyCode: String,
    val note: String = "",
    val createTime: Instant
)
