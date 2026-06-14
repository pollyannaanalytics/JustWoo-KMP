package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import com.pollyannawu.justwoo.model.ApiResult
import kotlin.math.roundToInt

sealed interface CreateSettlementResult {
    data object Success : CreateSettlementResult
    data class PartialFailure(val failedMemberIds: List<Long>) : CreateSettlementResult
    data class Failure(val message: String) : CreateSettlementResult
}

class CreateSettlementUseCase(
    private val settlementRepository: SettlementRepository,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
    private val getHouseMembers: GetHouseMembersUseCase,
) {
    suspend operator fun invoke(
        payerId: Long,
        payeeId: Long?,
        amount: Double,
        currencyCode: String,
        note: String,
    ): CreateSettlementResult {
        val houseId = getCurrentHouseId()
            ?: return CreateSettlementResult.Failure("No active house")

        return if (payeeId != null) {
            createSingle(houseId, payerId, payeeId, amount, currencyCode, note)
        } else {
            createHouseWide(houseId, payerId, amount, currencyCode, note)
        }
    }

    private suspend fun createSingle(
        houseId: Long,
        payerId: Long,
        payeeId: Long,
        amount: Double,
        currencyCode: String,
        note: String,
    ): CreateSettlementResult {
        val result = settlementRepository.createSettlement(
            houseId,
            CreateSettlementRequest(payerId = payerId, payeeId = payeeId, amount = amount, currencyCode = currencyCode, note = note),
        )
        return when (result) {
            is ApiResult.Success -> CreateSettlementResult.Success
            is ApiResult.Error -> CreateSettlementResult.Failure(result.exception.message ?: "Unknown error")
            ApiResult.Loading -> CreateSettlementResult.Failure("Unexpected loading state")
        }
    }

    private suspend fun createHouseWide(
        houseId: Long,
        payerId: Long,
        amount: Double,
        currencyCode: String,
        note: String,
    ): CreateSettlementResult {
        val members = getHouseMembers(houseId).filter { it.userId != payerId }
        if (members.isEmpty()) return CreateSettlementResult.Success

        val splitAmount = (amount / members.size * 100).roundToInt() / 100.0
        val remainder = (amount * 100).roundToInt() - (splitAmount * 100).roundToInt() * members.size

        val failedNames = mutableListOf<Long>()
        members.forEachIndexed { index, member ->
            val memberAmount = if (index == members.lastIndex) {
                splitAmount + remainder / 100.0
            } else {
                splitAmount
            }
            val result = settlementRepository.createSettlement(
                houseId,
                CreateSettlementRequest(
                    payerId = payerId,
                    payeeId = member.userId,
                    amount = memberAmount,
                    currencyCode = currencyCode,
                    note = note,
                ),
            )
            if (result is ApiResult.Error) failedNames.add(member.userId)
        }

        return if (failedNames.isEmpty()) CreateSettlementResult.Success
        else CreateSettlementResult.PartialFailure(failedNames)
    }
}
