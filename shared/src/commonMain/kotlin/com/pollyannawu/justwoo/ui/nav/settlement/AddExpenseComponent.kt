package com.pollyannawu.justwoo.ui.nav.settlement

import com.arkivanov.decompose.ComponentContext

interface AddExpenseComponent : ComponentContext {
    fun onClose()
    fun onSaved()
    fun onOpenCurrencyPicker(onResult: (String) -> Unit)
}

class DefaultAddExpenseComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
    private val onExpenseSaved: () -> Unit,
    private val onNavigateToCurrencyPicker: ((String) -> Unit) -> Unit,
) : AddExpenseComponent, ComponentContext by componentContext {

    override fun onClose() = onFinished()
    override fun onSaved() = onExpenseSaved()
    override fun onOpenCurrencyPicker(onResult: (String) -> Unit) = onNavigateToCurrencyPicker(onResult)
}
