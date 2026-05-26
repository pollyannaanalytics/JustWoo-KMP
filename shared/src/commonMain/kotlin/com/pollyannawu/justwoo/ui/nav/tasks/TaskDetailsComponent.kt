package com.pollyannawu.justwoo.ui.nav.tasks

import com.arkivanov.decompose.ComponentContext

interface TaskDetailsComponent {
    val taskId: Long
    fun onClose()
}

class DefaultTaskDetailsComponent(
    componentContext: ComponentContext,
    override val taskId: Long,
    private val onFinished: () -> Unit,
) : TaskDetailsComponent, ComponentContext by componentContext {
    override fun onClose() = onFinished()
}
