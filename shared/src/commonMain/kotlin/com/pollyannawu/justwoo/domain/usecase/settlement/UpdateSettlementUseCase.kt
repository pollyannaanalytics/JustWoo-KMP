package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.UpdateSettlementRequest
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase

sealed interface UpdateSettlementResult {
    data object Success : UpdateSettlementResult
    data class Failure(val message: String) : UpdateSettlementResult
}

class UpdateSettlementUseCase(
    private val settlementRepository: SettlementRepository,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
) {
    suspend operator fun invoke(
        settlementId: Long,
        payerId: Long,
        payeeId: Long,
        amount: Double,
        currencyCode: String,
        note: String,
    ): UpdateSettlementResult {
        val houseId = getCurrentHouseId()
            ?: return UpdateSettlementResult.Failure("No active house")

        return settlementRepository.updateSettlement(
            houseId,
            settlementId,
            UpdateSettlementRequest(payerId = payerId, payeeId = payeeId, amount = amount, currencyCode = currencyCode, note = note),
        ).fold(
            onSuccess = { UpdateSettlementResult.Success },
            onFailure = { UpdateSettlementResult.Failure(it.message ?: "Unknown error") },
        )
    }
}
