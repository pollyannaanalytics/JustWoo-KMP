package com.pollyannawu.justwoo.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpConfirmRequest(val code: String, val displayNumber: Int, val houseId: Long)
