package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import kotlinx.coroutines.flow.first

data class EditExpenseInitData(
    val currentUserId: Long,
    val allMembers: List<HouseMember>,
    val existingSettlement: Settlement?,
)

sealed interface LoadEditExpenseResult {
    data class Success(val data: EditExpenseInitData) : LoadEditExpenseResult
    data class Failure(val message: String) : LoadEditExpenseResult
}

class LoadEditExpenseUseCase(
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
    private val observeCurrentUserId: ObserveCurrentUserIdUseCase,
    private val getHouseMembers: GetHouseMembersUseCase,
    private val getSettlementById: GetSettlementByIdUseCase,
) {
    suspend operator fun invoke(settlementId: Long?): LoadEditExpenseResult {
        val houseId = getCurrentHouseId() ?: return LoadEditExpenseResult.Failure("No active house")
        val currentUserId = observeCurrentUserId().first()
            ?: return LoadEditExpenseResult.Failure("Not signed in")

        val allMembers = getHouseMembers(houseId)
        val existingSettlement = if (settlementId != null) getSettlementById(settlementId) else null

        return LoadEditExpenseResult.Success(
            EditExpenseInitData(
                currentUserId = currentUserId,
                allMembers = allMembers,
                existingSettlement = existingSettlement,
            )
        )
    }
}
