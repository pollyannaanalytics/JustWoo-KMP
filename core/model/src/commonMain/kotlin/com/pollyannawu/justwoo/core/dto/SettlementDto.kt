package com.pollyannawu.justwoo.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSettlementRequest(
    val payerId: Long,
    val payeeId: Long,
    val amount: Double,
    // ISO 4217 currency code, e.g. "TWD", "USD"
    val currencyCode: String,
    val note: String = ""
)

@Serializable
data class SettlementResponse(
    val id: Long,
    val houseId: Long,
    val payerId: Long,
    val payerName: String,
    val payeeId: Long,
    val payeeName: String,
    val amount: Double,
    val currencyCode: String,
    val note: String,
    val createTime: String
)

@Serializable
data class BalanceEntry(
    val userId: Long,
    val userName: String,
    val counterpartId: Long,
    val counterpartName: String,
    // Positive = userId owes counterpartId, in currencyCode (no TWD conversion)
    val netAmount: Double,
    val currencyCode: String
)

@Serializable
data class HouseBalanceResponse(
    val houseId: Long,
    val balances: List<BalanceEntry>
)
