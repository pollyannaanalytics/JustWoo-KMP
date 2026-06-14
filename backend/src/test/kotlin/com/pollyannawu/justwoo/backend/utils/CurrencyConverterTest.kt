package com.pollyannawu.justwoo.backend.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.math.abs

class CurrencyConverterTest {

    @Test
    fun `validate accepts valid ISO 4217 codes`() {
        listOf("TWD", "USD", "JPY", "EUR", "GBP").forEach { code ->
            CurrencyConverter.validate(code)  // should not throw
        }
    }

    @Test
    fun `validate throws UnknownCurrencyException for garbage code`() {
        assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.validate("XYZ")
        }
    }

    @Test
    fun `validate throws UnknownCurrencyException for empty string`() {
        assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.validate("")
        }
    }

    @Test
    fun `convert same currency returns same amount`() {
        val result = CurrencyConverter.convert(100.0, "TWD", "TWD")
        assertApprox(100.0, result)
    }

    @Test
    fun `convert TWD to USD then back returns original amount`() {
        val usd = CurrencyConverter.convert(320.0, "TWD", "USD")
        val twd = CurrencyConverter.convert(usd, "USD", "TWD")
        assertApprox(320.0, twd)
    }

    @Test
    fun `toTwd converts correctly`() {
        // 1 USD = 32 TWD per the hardcoded rate
        val result = CurrencyConverter.toTwd(1.0, "USD")
        assertApprox(32.0, result)
    }

    @Test
    fun `convert throws UnknownCurrencyException for unknown from-currency`() {
        assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.convert(100.0, "ZZZ", "TWD")
        }
    }

    @Test
    fun `convert throws UnknownCurrencyException for unknown to-currency`() {
        assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.convert(100.0, "TWD", "ZZZ")
        }
    }

    @Test
    fun `UnknownCurrencyException message contains the bad code`() {
        val ex = assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.validate("BADCODE")
        }
        assert(ex.message?.contains("BADCODE") == true)
    }

    private fun assertApprox(expected: Double, actual: Double, epsilon: Double = 0.001) {
        assert(abs(expected - actual) < epsilon) {
            "Expected ≈$expected but was $actual"
        }
    }
}
