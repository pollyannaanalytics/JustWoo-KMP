package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class ObserveSettlementsUseCase(
    private val settlementRepository: SettlementRepository,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
) {
    operator fun invoke(): Flow<List<Settlement>> {
        val houseId = getCurrentHouseId() ?: return emptyFlow()
        return settlementRepository.observeSettlements(houseId)
    }
}
