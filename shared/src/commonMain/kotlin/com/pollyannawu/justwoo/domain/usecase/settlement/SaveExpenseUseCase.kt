package com.pollyannawu.justwoo.domain.usecase.settlement

sealed interface SaveExpenseResult {
    data object Success : SaveExpenseResult
    data class PartialFailure(val failedPayeeIds: List<Long>) : SaveExpenseResult
    data class Failure(val message: String) : SaveExpenseResult
}

class SaveExpenseUseCase(
    private val createSettlement: CreateSettlementUseCase,
    private val updateSettlement: UpdateSettlementUseCase,
) {
    /** Create a new expense split across [payeeIds]. */
    suspend fun create(
        payerId: Long,
        payeeIds: Set<Long>,
        amount: Double,
        currencyCode: String,
        note: String,
    ): SaveExpenseResult = when (val r = createSettlement(payerId, payeeIds, amount, currencyCode, note)) {
        CreateSettlementResult.Success -> SaveExpenseResult.Success
        is CreateSettlementResult.PartialFailure -> SaveExpenseResult.PartialFailure(r.failedMemberIds)
        is CreateSettlementResult.Failure -> SaveExpenseResult.Failure(r.message)
    }

    /**
     * Update an existing settlement record for the first payee and create new records for any
     * additional payees, splitting [amount] evenly (remainder goes to the first payee).
     */
    suspend fun update(
        settlementId: Long,
        payerId: Long,
        payeeIds: List<Long>,
        amount: Double,
        currencyCode: String,
        note: String,
    ): SaveExpenseResult {
        if (payeeIds.isEmpty()) return SaveExpenseResult.Failure("Select a payee")

        val splitAmount = (amount / payeeIds.size * 100).toLong() / 100.0
        val remainder = (amount * 100).toLong() - (splitAmount * 100).toLong() * payeeIds.size

        val firstResult = updateSettlement(
            settlementId = settlementId,
            payerId = payerId,
            payeeId = payeeIds.first(),
            amount = splitAmount + remainder / 100.0,
            currencyCode = currencyCode,
            note = note,
        )
        if (firstResult is UpdateSettlementResult.Failure) {
            return SaveExpenseResult.Failure(firstResult.message)
        }

        if (payeeIds.size == 1) return SaveExpenseResult.Success

        val failedIds = mutableListOf<Long>()
        payeeIds.drop(1).forEach { payeeId ->
            val result = createSettlement(
                payerId = payerId,
                payeeIds = setOf(payeeId),
                amount = splitAmount,
                currencyCode = currencyCode,
                note = note,
            )
            if (result is CreateSettlementResult.Failure || result is CreateSettlementResult.PartialFailure) {
                failedIds.add(payeeId)
            }
        }

        return if (failedIds.isEmpty()) SaveExpenseResult.Success
        else SaveExpenseResult.PartialFailure(failedIds)
    }
}
