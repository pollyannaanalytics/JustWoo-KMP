package com.pollyannawu.justwoo.ui.nav.profile

import com.arkivanov.decompose.ComponentContext

interface ProfileComponent : ComponentContext {
    fun onClose()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
) : ProfileComponent, ComponentContext by componentContext {

    override fun onClose() = onFinished()
}
