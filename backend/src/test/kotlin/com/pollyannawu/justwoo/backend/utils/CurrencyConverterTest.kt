package com.pollyannawu.justwoo.backend.utils

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CurrencyConverterTest {

    @Test
    fun `validate accepts any 3-letter uppercase ISO 4217 format code`() {
        listOf("TWD", "USD", "JPY", "EUR", "GBP", "CAD", "AUD", "CNY", "AMD", "XYZ").forEach { code ->
            CurrencyConverter.validate(code)
        }
    }

    @Test
    fun `validate throws for empty string`() {
        assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.validate("")
        }
    }

    @Test
    fun `validate throws for lowercase code`() {
        assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.validate("usd")
        }
    }

    @Test
    fun `validate throws for code longer than 3 letters`() {
        assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.validate("BADCODE")
        }
    }

    @Test
    fun `validate throws for numeric or symbol code`() {
        listOf("123", "US$", "US").forEach { code ->
            assertThrows(UnknownCurrencyException::class.java) {
                CurrencyConverter.validate(code)
            }
        }
    }

    @Test
    fun `UnknownCurrencyException code field and message contain the bad code`() {
        val ex = assertThrows(UnknownCurrencyException::class.java) {
            CurrencyConverter.validate("TOOLONG")
        }
        assert(ex.code == "TOOLONG")
        assert(ex.message?.contains("TOOLONG") == true)
    }
}
