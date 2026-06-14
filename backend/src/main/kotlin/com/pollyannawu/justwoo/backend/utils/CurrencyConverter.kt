package com.pollyannawu.justwoo.backend.utils

class UnknownCurrencyException(val code: String) :
    IllegalArgumentException("Unknown or unsupported currency code: $code")

private val ISO4217_REGEX = Regex("^[A-Z]{3}$")

object CurrencyConverter {

    // Validates ISO 4217 format (3 uppercase letters).
    // Avoids Currency.getInstance() which rejects valid codes on minimal JRE images.
    fun validate(code: String) {
        if (!code.matches(ISO4217_REGEX)) throw UnknownCurrencyException(code)
    }
}
