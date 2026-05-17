package com.pollyannawu.justwoo.ui.nav.tasks

import com.arkivanov.decompose.ComponentContext

interface AssignedTasksComponent {
    val assigneeId: Long
    fun onClose()
}

class DefaultAssignedTasksComponent(
    componentContext: ComponentContext,
    override val assigneeId: Long,
    private val onFinished: () -> Unit,
) : AssignedTasksComponent, ComponentContext by componentContext {

    override fun onClose() = onFinished()
}
