package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase

fun interface GetHouseBalanceUseCase {
    suspend operator fun invoke(): Result<HouseBalanceResponse>
}

class DefaultGetHouseBalanceUseCase(
    private val settlementRepository: SettlementRepository,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
) : GetHouseBalanceUseCase {
    override suspend fun invoke(): Result<HouseBalanceResponse> {
        val houseId = getCurrentHouseId()
            ?: return Result.failure(Exception("No active house"))
        return settlementRepository.getBalance(houseId)
    }
}
