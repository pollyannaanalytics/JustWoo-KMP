package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.model.ApiResult

class GetHouseBalanceUseCase(
    private val settlementRepository: SettlementRepository,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
) {
    suspend operator fun invoke(): ApiResult<HouseBalanceResponse> {
        val houseId = getCurrentHouseId()
            ?: return ApiResult.Error(Exception("No active house"))
        return settlementRepository.getBalance(houseId)
    }
}
