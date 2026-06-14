package com.pollyannawu.justwoo.ui.nav.profile

import com.arkivanov.decompose.ComponentContext

interface ProfileViewComponent {
    fun onEdit()
    fun onClose()
}

class DefaultProfileViewComponent(
    componentContext: ComponentContext,
    private val onEdit: () -> Unit,
    private val onClose: () -> Unit,
) : ProfileViewComponent, ComponentContext by componentContext {

    override fun onEdit() = onEdit.invoke()
    override fun onClose() = onClose.invoke()
}
