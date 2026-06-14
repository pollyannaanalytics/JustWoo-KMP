package com.pollyannawu.justwoo.ui.nav.tasks

import com.arkivanov.decompose.ComponentContext

interface TaskEditComponent {
    val editingTaskId: Long?

    fun onSave()
    fun onCancel()
}

class DefaultTaskEditComponent(
    componentContext: ComponentContext,
    override val editingTaskId: Long? = null,
    private val onFinished: () -> Unit,
) : TaskEditComponent, ComponentContext by componentContext {

    override fun onSave() = onFinished()
    override fun onCancel() = onFinished()
}
