package com.pollyannawu.justwoo.domain.currency

/** Returns all available ISO 4217 currencies sorted by code, with symbol and English name. */
expect fun availableCurrencies(): List<CurrencyInfo>
