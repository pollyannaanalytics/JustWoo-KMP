package com.pollyannawu.justwoo.ui.nav.tasks

import com.arkivanov.decompose.ComponentContext

/**
 * 浮在 stack 之上的快速 status 卡。由 RootComponent 的 ChildSlot 管理。
 *  - onBack: 純關閉，不改 status
 *  - onComplete: ViewModel 標記完成後 → dismiss
 */
interface TaskQuickStatusComponent {
    val taskId: Long

    fun onBack()
    fun onComplete()
}

class DefaultTaskQuickStatusComponent(
    componentContext: ComponentContext,
    override val taskId: Long,
    private val onDismiss: () -> Unit,
) : TaskQuickStatusComponent, ComponentContext by componentContext {

    override fun onBack() = onDismiss()
    override fun onComplete() = onDismiss()
}
