package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import com.pollyannawu.justwoo.core.dto.UpdateSettlementRequest
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.SettlementRepository
import com.pollyannawu.justwoo.model.AuthDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant

internal fun stubSettlementResponse(id: Long = 1L) = SettlementResponse(
    id = id, houseId = 1L, payerId = 1L, payerName = "Payer",
    payeeId = 2L, payeeName = "Payee", amount = 100.0, currencyCode = "TWD",
    note = "", createTime = "2024-01-01T00:00:00Z",
)

internal fun stubSettlement(id: Long = 1L) = Settlement(
    id = id, houseId = 1L, payerId = 1L, payeeId = 2L,
    amount = 100.0, currencyCode = "TWD",
    createTime = Instant.fromEpochMilliseconds(0),
)

internal fun stubMember(userId: Long, houseId: Long = 1L) = HouseMember(
    houseId = houseId, userId = userId, name = "Member $userId",
    role = MemberRole.MEMBER, joinedAt = Instant.fromEpochMilliseconds(0),
)

internal class FakeSettlementRepository(
    var balance: Result<HouseBalanceResponse> =
        Result.success(HouseBalanceResponse(1L, emptyList())),
    var createFn: suspend (Long, CreateSettlementRequest) -> Result<SettlementResponse> =
        { _, _ -> Result.success(stubSettlementResponse()) },
    var updateFn: suspend (Long, Long, UpdateSettlementRequest) -> Result<SettlementResponse> =
        { _, _, _ -> Result.success(stubSettlementResponse()) },
    var settlementById: Settlement? = stubSettlement(),
    private val settlementsFlow: Flow<List<Settlement>> = flowOf(emptyList()),
    val synced: MutableList<Long> = mutableListOf(),
    val created: MutableList<CreateSettlementRequest> = mutableListOf(),
    val updated: MutableList<UpdateSettlementRequest> = mutableListOf(),
) : SettlementRepository {
    override fun observeSettlements(houseId: Long): Flow<List<Settlement>> = settlementsFlow
    override suspend fun syncSettlements(houseId: Long) { synced += houseId }
    override suspend fun getBalance(houseId: Long): Result<HouseBalanceResponse> = balance
    override suspend fun createSettlement(houseId: Long, request: CreateSettlementRequest): Result<SettlementResponse> {
        created += request
        return createFn(houseId, request)
    }
    override suspend fun updateSettlement(houseId: Long, settlementId: Long, request: UpdateSettlementRequest): Result<SettlementResponse> {
        updated += request
        return updateFn(houseId, settlementId, request)
    }
    override suspend fun getSettlementById(id: Long): Settlement? = settlementById
}

internal class StubAuthRepository(private val houseId: Long? = 1L) : AuthRepository {
    override val currentUserId: Flow<Long?> = flowOf(null)
    override val currentHouseId: Flow<Long?> = flowOf(houseId)
    override val currentUserEmail: Flow<String?> = flowOf(null)
    override val isAuthenticated: Flow<Boolean> = flowOf(false)
    override fun hasActiveSession(): Boolean = false
    override fun getCurrentHouseId(): Long? = houseId
    override fun hasOnboarded(): Boolean = false
    override fun setCurrentHouseId(houseId: Long) {}
    override suspend fun register(email: String, password: String): AuthDataResult = throw UnsupportedOperationException()
    override suspend fun login(email: String, password: String): AuthDataResult = throw UnsupportedOperationException()
    override suspend fun logout() {}
    override suspend fun changePassword(oldPassword: String, newPassword: String): Boolean = false
}

internal class StubHouseRepository(
    private val houseId: Long = 1L,
    private val members: List<HouseMember> = emptyList(),
) : HouseRepository {
    override fun observeHouses(): Flow<List<House>> = flowOf(
        listOf(
            House(
                id = houseId, name = "Test House", avatar = "", description = "",
                members = members,
                createTime = Instant.fromEpochMilliseconds(0),
                updateTime = Instant.fromEpochMilliseconds(0),
            )
        )
    )
    override suspend fun refreshHouses(page: Int) {}
    override suspend fun createHouse(house: House) {}
    override suspend fun updateHouse(house: House) {}
    override suspend fun addMember(houseId: Long, memberId: Long) {}
    override suspend fun removeMember(houseId: Long, memberId: Long) {}
    override suspend fun fetchFirstHouseId(): Long? = null
}
