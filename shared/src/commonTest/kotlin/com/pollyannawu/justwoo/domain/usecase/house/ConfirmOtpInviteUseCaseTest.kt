package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.OtpConfirmRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ConfirmOtpInviteUseCaseTest {

    private val request = OtpConfirmRequest(code = "ABCDEF", displayNumber = 3, houseId = 10L)

    @Test
    fun `invoke completes normally when repository does not throw`() = runTest {
        val repo = FakeHouseInviteRepository(confirmOtpInviteError = null)
        val useCase = ConfirmOtpInviteUseCase(repo)
        useCase(request.code, request.displayNumber, request.houseId)
        // no exception = success
    }

    @Test
    fun `invoke propagates exception when repository throws`() = runTest {
        val error = RuntimeException("number mismatch")
        val repo = FakeHouseInviteRepository(confirmOtpInviteError = error)
        val useCase = ConfirmOtpInviteUseCase(repo)
        assertFailsWith<RuntimeException> {
            useCase(request.code, request.displayNumber, request.houseId)
        }
    }
}
