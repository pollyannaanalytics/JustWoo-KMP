package com.pollyannawu.justwoo.ui.nav.settlement

import com.arkivanov.decompose.ComponentContext

interface SettlementComponent : ComponentContext {
    fun onClose()
    fun onAddExpense()
    fun onEditSettlement(settlementId: Long)
}

class DefaultSettlementComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
    private val onNavigateToAddExpense: () -> Unit,
    private val onNavigateToEditExpense: (Long) -> Unit,
) : SettlementComponent, ComponentContext by componentContext {

    override fun onClose() = onFinished()
    override fun onAddExpense() = onNavigateToAddExpense()
    override fun onEditSettlement(settlementId: Long) = onNavigateToEditExpense(settlementId)
}
