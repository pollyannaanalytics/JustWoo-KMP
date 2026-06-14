package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.repositories.SettlementRepository
import com.pollyannawu.justwoo.backend.utils.CurrencyConverter
import com.pollyannawu.justwoo.backend.utils.UnknownCurrencyException
import com.pollyannawu.justwoo.backend.utils.dataresult.SettlementDataResult

import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.dto.BalanceEntry
import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import kotlinx.datetime.Clock

interface SettlementService {
    suspend fun createSettlement(
        houseId: Long,
        requesterId: Long,
        request: CreateSettlementRequest
    ): SettlementDataResult<SettlementResponse>

    suspend fun getSettlements(
        houseId: Long,
        requesterId: Long
    ): SettlementDataResult<List<SettlementResponse>>

    suspend fun getHouseBalance(
        houseId: Long,
        requesterId: Long,
    ): SettlementDataResult<HouseBalanceResponse>
}

private data class DebtPair(val debtorId: Long, val creditorId: Long)

internal class DefaultSettlementService(
    private val houseRepo: HouseRepository,
    private val settlementRepo: SettlementRepository,
    private val profileRepo: ProfileRepository
) : SettlementService {

    override suspend fun createSettlement(
        houseId: Long,
        requesterId: Long,
        request: CreateSettlementRequest
    ): SettlementDataResult<SettlementResponse> {
        if (!houseRepo.isMember(requesterId, houseId))
            return SettlementDataResult.Error.UserNotAllowed(requesterId)
        if (request.amount <= 0)
            return SettlementDataResult.Error.InvalidAmount
        try {
            CurrencyConverter.validate(request.currencyCode)
        } catch (_: UnknownCurrencyException) {
            return SettlementDataResult.Error.InvalidCurrency(request.currencyCode)
        }

        return try {
            val settlement = Settlement(
                houseId = houseId,
                payerId = request.payerId,
                payeeId = request.payeeId,
                amount = request.amount,
                currencyCode = request.currencyCode,
                note = request.note,
                createTime = Clock.System.now()
            )
            val saved = settlementRepo.createSettlement(settlement)
            val profiles = profileRepo.getProfiles(listOf(saved.payerId, saved.payeeId))
                .associateBy { it.id }
            SettlementDataResult.Success(saved.toResponse(profiles))
        } catch (e: Exception) {
            SettlementDataResult.Error.DatabaseError(e.message ?: "Unknown error")
        }
    }

    override suspend fun getSettlements(
        houseId: Long,
        requesterId: Long
    ): SettlementDataResult<List<SettlementResponse>> {
        if (!houseRepo.isMember(requesterId, houseId))
            return SettlementDataResult.Error.UserNotAllowed(requesterId)

        return try {
            val settlements = settlementRepo.getSettlements(houseId)
            val userIds = settlements.flatMap { listOf(it.payerId, it.payeeId) }.distinct()
            val profiles = profileRepo.getProfiles(userIds).associateBy { it.id }
            SettlementDataResult.Success(settlements.map { it.toResponse(profiles) })
        } catch (e: Exception) {
            SettlementDataResult.Error.DatabaseError(e.message ?: "Unknown error")
        }
    }

    override suspend fun getHouseBalance(
        houseId: Long,
        requesterId: Long,
    ): SettlementDataResult<HouseBalanceResponse> {
        if (!houseRepo.isMember(requesterId, houseId))
            return SettlementDataResult.Error.UserNotAllowed(requesterId)

        return try {
            val debts = buildDebtMap(settlementRepo.getTasksWithPrice(houseId))
            val payments = buildPaymentMap(settlementRepo.getSettlements(houseId))
            val net = netBalance(debts, payments)

            val userIds = net.keys.flatMap { listOf(it.debtorId, it.creditorId) }.distinct()
            val profiles = profileRepo.getProfiles(userIds).associateBy { it.id }

            val balances = net.flatMap { (pair, amountByCurrency) ->
                amountByCurrency.map { (currencyCode, amount) ->
                    BalanceEntry(
                        userId = pair.debtorId,
                        userName = profiles[pair.debtorId]?.name ?: "",
                        counterpartId = pair.creditorId,
                        counterpartName = profiles[pair.creditorId]?.name ?: "",
                        netAmount = amount,
                        currencyCode = currencyCode,
                    )
                }
            }

            SettlementDataResult.Success(HouseBalanceResponse(houseId, balances))
        } catch (e: Exception) {
            SettlementDataResult.Error.DatabaseError(e.message ?: "Unknown error")
        }
    }

    // each non-owner assignee owes the owner their equal share of the task price
    private fun buildDebtMap(tasks: List<Task>): Map<DebtPair, Map<String, Double>> {
        val debts = mutableMapOf<DebtPair, MutableMap<String, Double>>()
        for (task in tasks) {
            val price = task.price ?: continue
            val code = task.currencyCode ?: continue
            val nonOwnerAssignees = task.assignees.map { it.userId }.filter { it != task.ownerId }
            if (nonOwnerAssignees.isEmpty()) continue
            val share = price / nonOwnerAssignees.size
            for (assigneeId in nonOwnerAssignees) {
                debts.getOrPut(DebtPair(assigneeId, task.ownerId)) { mutableMapOf() }.merge(code, share, Double::plus)
            }
        }
        return debts
    }

    // total paid per (payer, payee), keyed by currency
    private fun buildPaymentMap(settlements: List<Settlement>): Map<DebtPair, Map<String, Double>> {
        val payments = mutableMapOf<DebtPair, MutableMap<String, Double>>()
        for (s in settlements) {
            payments.getOrPut(DebtPair(s.payerId, s.payeeId)) { mutableMapOf() }
                .merge(s.currencyCode, s.amount, Double::plus)
        }
        return payments
    }

    // Collapse bilateral debts and payments into one canonical net entry per (pair, currency).
    // canonical(A,B) = DebtPair with smaller ID as debtor; the other direction is the reverse.
    private fun netBalance(
        debts: Map<DebtPair, Map<String, Double>>,
        payments: Map<DebtPair, Map<String, Double>>,
    ): Map<DebtPair, Map<String, Double>> {
        val canonicalPairs = (debts.keys + payments.keys).map { canonical(it) }.toSet()
        val result = mutableMapOf<DebtPair, MutableMap<String, Double>>()

        for (pair in canonicalPairs) {
            val reverse = DebtPair(pair.creditorId, pair.debtorId)
            val currencies = setOf(
                debts[pair]?.keys, debts[reverse]?.keys,
                payments[pair]?.keys, payments[reverse]?.keys,
            ).filterNotNull().flatten().toSet()

            for (currency in currencies) {
                val netDebt = (debts[pair]?.get(currency) ?: 0.0) - (debts[reverse]?.get(currency) ?: 0.0)
                // Any payment between A and B (either direction) reduces the outstanding balance
                val totalPaid = (payments[pair]?.get(currency) ?: 0.0) + (payments[reverse]?.get(currency) ?: 0.0)
                val remaining = netDebt - totalPaid
                when {
                    remaining > 0.01 -> result.getOrPut(pair) { mutableMapOf() }[currency] = remaining
                    remaining < -0.01 -> result.getOrPut(reverse) { mutableMapOf() }[currency] = -remaining
                }
            }
        }
        return result
    }

    private fun canonical(pair: DebtPair): DebtPair =
        if (pair.debtorId <= pair.creditorId) pair else DebtPair(pair.creditorId, pair.debtorId)

    private fun Settlement.toResponse(profiles: Map<Long, Profile>): SettlementResponse =
        SettlementResponse(
            id = id,
            houseId = houseId,
            payerId = payerId,
            payerName = profiles[payerId]?.name ?: "",
            payeeId = payeeId,
            payeeName = profiles[payeeId]?.name ?: "",
            amount = amount,
            currencyCode = currencyCode,
            note = note,
            createTime = createTime.toString()
        )
}
