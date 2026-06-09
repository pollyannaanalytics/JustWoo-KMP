package com.pollyannawu.justwoo.ui.nav.settlement

import com.arkivanov.decompose.ComponentContext

interface SettlementComponent {
    fun onClose()
    fun onAddExpense()
}

class DefaultSettlementComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
    private val onNavigateToAddExpense: () -> Unit,
) : SettlementComponent, ComponentContext by componentContext {

    override fun onClose() = onFinished()
    override fun onAddExpense() = onNavigateToAddExpense()
}
