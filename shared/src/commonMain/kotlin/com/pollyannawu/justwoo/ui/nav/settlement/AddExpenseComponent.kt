package com.pollyannawu.justwoo.ui.nav.settlement

import com.arkivanov.decompose.ComponentContext

interface AddExpenseComponent {
    fun onClose()
    fun onSaved()
}

class DefaultAddExpenseComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
    private val onExpenseSaved: () -> Unit,
) : AddExpenseComponent, ComponentContext by componentContext {

    override fun onClose() = onFinished()
    override fun onSaved() = onExpenseSaved()
}
