package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.data.SettlementRepository

class GetSettlementByIdUseCase(
    private val settlementRepository: SettlementRepository,
) {
    suspend operator fun invoke(settlementId: Long): Settlement? =
        settlementRepository.getSettlementById(settlementId)
}
