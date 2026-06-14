package com.pollyannawu.justwoo.backend.utils

import java.util.Currency

class UnknownCurrencyException(code: String) :
    IllegalArgumentException("Unknown or unsupported currency code: \"$code\"")

object CurrencyConverter {

    // Approximate TWD equivalent for 1 unit of each currency.
    // Replace with a live rates feed for production use.
    private val ratesInTwd: Map<String, Double> = mapOf(
        "TWD" to 1.0,
        "USD" to 32.0,
        "JPY" to 0.22,
        "EUR" to 34.5,
        "CNY" to 4.4,
        "HKD" to 4.1,
        "SGD" to 23.5,
        "KRW" to 0.024,
        "GBP" to 40.5,
        "AUD" to 20.5
    )

    // Validates that the code is a real ISO 4217 currency recognised by the JVM.
    fun validate(code: String) {
        try {
            Currency.getInstance(code)
        } catch (_: IllegalArgumentException) {
            throw UnknownCurrencyException(code)
        }
    }

    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        if (fromCode == toCode) return amount
        val fromRate = ratesInTwd[fromCode]
            ?: throw UnknownCurrencyException(fromCode)
        val toRate = ratesInTwd[toCode]
            ?: throw UnknownCurrencyException(toCode)
        return amount * fromRate / toRate
    }

    fun toTwd(amount: Double, currencyCode: String): Double = convert(amount, currencyCode, "TWD")
}
