package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase

class SyncSettlementsUseCase(
    private val settlementRepository: SettlementRepository,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
) {
    suspend operator fun invoke() {
        val houseId = getCurrentHouseId() ?: return
        settlementRepository.syncSettlements(houseId)
    }
}
