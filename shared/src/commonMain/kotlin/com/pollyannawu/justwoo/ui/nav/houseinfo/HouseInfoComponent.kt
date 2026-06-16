package com.pollyannawu.justwoo.ui.nav.houseinfo

import com.arkivanov.decompose.ComponentContext

interface HouseInfoComponent : ComponentContext {
    fun onClose()
}

class DefaultHouseInfoComponent(
    componentContext: ComponentContext,
    private val onClose: () -> Unit,
) : HouseInfoComponent, ComponentContext by componentContext {

    override fun onClose() = onClose.invoke()
}
