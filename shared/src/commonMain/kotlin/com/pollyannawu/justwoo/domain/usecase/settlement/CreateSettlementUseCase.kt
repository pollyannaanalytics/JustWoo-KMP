package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase

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
        payeeIds: Set<Long>,
        amount: Double,
        currencyCode: String,
        note: String,
    ): CreateSettlementResult {
        val houseId = getCurrentHouseId()
            ?: return CreateSettlementResult.Failure("No active house")

        val targetIds = (payeeIds - payerId)

        return if (targetIds.size == 1) {
            createSingle(houseId, payerId, targetIds.first(), amount, currencyCode, note)
        } else if (targetIds.isEmpty()) {
            createHouseWide(houseId, payerId, amount, currencyCode, note)
        } else {
            createSplit(houseId, payerId, targetIds, amount, currencyCode, note)
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
        val otherMemberIds = members.filter { it.userId != payerId }.map { it.userId }
        if (otherMemberIds.isEmpty()) return CreateSettlementResult.Success
        return createSplit(houseId, payerId, otherMemberIds.toSet(), amount, currencyCode, note)
    }

    private suspend fun createSplit(
        houseId: Long,
        payerId: Long,
        payeeIds: Set<Long>,
        amount: Double,
        currencyCode: String,
        note: String,
    ): CreateSettlementResult {
        val targets = payeeIds.toList()
        if (targets.isEmpty()) return CreateSettlementResult.Success

        val splitAmount = (amount / targets.size * 100).toLong() / 100.0
        val remainder = (amount * 100).toLong() - (splitAmount * 100).toLong() * targets.size

        val failedIds = mutableListOf<Long>()
        targets.forEachIndexed { index, memberId ->
            val memberAmount = if (index == targets.lastIndex) {
                splitAmount + remainder / 100.0
            } else {
                splitAmount
            }
            val result = settlementRepository.createSettlement(
                houseId,
                CreateSettlementRequest(
                    payerId = payerId,
                    payeeId = memberId,
                    amount = memberAmount,
                    currencyCode = currencyCode,
                    note = note,
                ),
            )
            if (result.isFailure) failedIds.add(memberId)
        }

        return if (failedIds.isEmpty()) CreateSettlementResult.Success
        else CreateSettlementResult.PartialFailure(failedIds)
    }
}
