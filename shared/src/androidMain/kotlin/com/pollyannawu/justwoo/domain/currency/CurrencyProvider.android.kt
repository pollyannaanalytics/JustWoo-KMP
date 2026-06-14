package com.pollyannawu.justwoo.domain.currency

import java.util.Currency
import java.util.Locale

actual fun availableCurrencies(): List<CurrencyInfo> =
    Currency.getAvailableCurrencies()
        .map { currency ->
            CurrencyInfo(
                code = currency.currencyCode,
                symbol = currency.getSymbol(Locale.getDefault()),
                name = currency.getDisplayName(Locale.ENGLISH),
            )
        }
        .sortedBy { it.code }
