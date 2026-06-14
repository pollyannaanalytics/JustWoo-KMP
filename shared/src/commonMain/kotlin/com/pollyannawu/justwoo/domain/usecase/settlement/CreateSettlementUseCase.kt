package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
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
    ): CreateSettlementResult =
        settlementRepository.createSettlement(
            houseId,
            CreateSettlementRequest(payerId = payerId, payeeId = payeeId, amount = amount, currencyCode = currencyCode, note = note),
        ).fold(
            onSuccess = { CreateSettlementResult.Success },
            onFailure = { CreateSettlementResult.Failure(it.message ?: "Unknown error") },
        )

    private suspend fun createHouseWide(
        houseId: Long,
        payerId: Long,
        amount: Double,
        currencyCode: String,
        note: String,
    ): CreateSettlementResult {
        val members = getHouseMembers(houseId)
        val otherMembers = members.filter { it.userId != payerId }
        if (otherMembers.isEmpty()) return CreateSettlementResult.Success

        val splitAmount = (amount / otherMembers.size * 100).roundToInt() / 100.0
        val remainder = (amount * 100).roundToInt() - (splitAmount * 100).roundToInt() * otherMembers.size

        val failedNames = mutableListOf<Long>()
        otherMembers.forEachIndexed { index, member ->
            val memberAmount = if (index == otherMembers.lastIndex) {
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
            if (result.isFailure) failedNames.add(member.userId)
        }

        return if (failedNames.isEmpty()) CreateSettlementResult.Success
        else CreateSettlementResult.PartialFailure(failedNames)
    }
}
