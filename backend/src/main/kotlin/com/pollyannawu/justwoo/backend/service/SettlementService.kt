package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.repositories.SettlementRepository
import com.pollyannawu.justwoo.backend.utils.CurrencyConverter
import com.pollyannawu.justwoo.backend.utils.UnknownCurrencyException
import com.pollyannawu.justwoo.backend.utils.dataresult.SettlementDataResult
import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.core.Settlement
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
        displayCurrencyCode: String
    ): SettlementDataResult<HouseBalanceResponse>
}

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
        displayCurrencyCode: String
    ): SettlementDataResult<HouseBalanceResponse> {
        if (!houseRepo.isMember(requesterId, houseId))
            return SettlementDataResult.Error.UserNotAllowed(requesterId)
        try {
            CurrencyConverter.validate(displayCurrencyCode)
        } catch (_: UnknownCurrencyException) {
            return SettlementDataResult.Error.InvalidCurrency(displayCurrencyCode)
        }

        return try {
            val tasks = settlementRepo.getTasksWithPrice(houseId)
            val net = mutableMapOf<Pair<Long, Long>, Double>()

            for (task in tasks) {
                val price = task.price ?: continue
                val code = task.currencyCode ?: continue
                val executorId = task.executorId.takeIf { it != 0L } ?: continue
                if (executorId == task.ownerId) continue
                // UnknownCurrencyException here means bad data in DB — let it bubble up
                val amountTwd = CurrencyConverter.toTwd(price, code)
                val key = Pair(executorId, task.ownerId)
                net[key] = (net[key] ?: 0.0) + amountTwd
            }

            for (s in settlementRepo.getSettlements(houseId)) {
                val amountTwd = CurrencyConverter.toTwd(s.amount, s.currencyCode)
                val key = Pair(s.payerId, s.payeeId)
                net[key] = (net[key] ?: 0.0) - amountTwd
                val current = net[key]!!
                if (current < 0) {
                    net.remove(key)
                    val reverseKey = Pair(s.payeeId, s.payerId)
                    net[reverseKey] = (net[reverseKey] ?: 0.0) + (-current)
                }
            }

            net.entries.removeIf { it.value < 0.01 }

            val userIds = net.keys.flatMap { listOf(it.first, it.second) }.distinct()
            val profiles = profileRepo.getProfiles(userIds).associateBy { it.id }

            val balances = net.map { (pair, amountTwd) ->
                val (debtorId, creditorId) = pair
                val displayAmount = CurrencyConverter.convert(amountTwd, "TWD", displayCurrencyCode)
                BalanceEntry(
                    userId = debtorId,
                    userName = profiles[debtorId]?.name ?: "",
                    counterpartId = creditorId,
                    counterpartName = profiles[creditorId]?.name ?: "",
                    netAmountTwd = amountTwd,
                    netAmount = displayAmount,
                    currencyCode = displayCurrencyCode
                )
            }

            SettlementDataResult.Success(HouseBalanceResponse(houseId, displayCurrencyCode, balances))
        } catch (e: UnknownCurrencyException) {
            SettlementDataResult.Error.InvalidCurrency(e.message ?: "")
        } catch (e: Exception) {
            SettlementDataResult.Error.DatabaseError(e.message ?: "Unknown error")
        }
    }

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
