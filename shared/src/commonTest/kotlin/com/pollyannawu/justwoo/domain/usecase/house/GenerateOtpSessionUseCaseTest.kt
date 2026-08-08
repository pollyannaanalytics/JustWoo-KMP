package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.OtpInviteSession
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GenerateOtpSessionUseCaseTest {

    private val stubSession = OtpInviteSession(
        code = "482931",
        displayNumber = 42,
        expiresInSeconds = 60,
    )

    @Test
    fun `success - returns OtpInviteSession from repository`() = runTest {
        val repo = FakeHouseInviteRepository(generateOtpSessionResult = stubSession)
        val result = GenerateOtpSessionUseCase(repo)()
        assertEquals(stubSession, result)
    }

    @Test
    fun `failure - propagates exception from repository`() = runTest {
        val error = RuntimeException("OTP generation failed")
        val repo = FakeHouseInviteRepository(generateOtpSessionError = error)
        assertFailsWith<RuntimeException> { GenerateOtpSessionUseCase(repo)() }
    }
}
