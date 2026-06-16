package com.pollyannawu.justwoo.ui.nav.houseinfo

import com.arkivanov.decompose.ComponentContext

interface HouseInfoComponent : ComponentContext {
    fun onClose()
    fun onInviteMember(houseId: Long)
}

class DefaultHouseInfoComponent(
    componentContext: ComponentContext,
    private val onClose: () -> Unit,
    private val onInviteMember: (Long) -> Unit = {},
) : HouseInfoComponent, ComponentContext by componentContext {

    override fun onClose() = onClose.invoke()
    override fun onInviteMember(houseId: Long) = onInviteMember.invoke(houseId)
}
