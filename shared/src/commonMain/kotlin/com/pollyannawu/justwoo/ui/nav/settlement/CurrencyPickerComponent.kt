package com.pollyannawu.justwoo.ui.nav.settlement

import com.arkivanov.decompose.ComponentContext

interface CurrencyPickerComponent {
    fun onClose()
    fun onCurrencySelected(code: String)
}

class DefaultCurrencyPickerComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
    private val onSelected: (String) -> Unit,
) : CurrencyPickerComponent, ComponentContext by componentContext {
    override fun onClose() = onFinished()
    override fun onCurrencySelected(code: String) = onSelected(code)
}
