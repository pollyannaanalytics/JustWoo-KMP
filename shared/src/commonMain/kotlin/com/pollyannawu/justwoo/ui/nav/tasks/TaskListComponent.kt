package com.pollyannawu.justwoo.ui.nav.tasks

import com.arkivanov.decompose.ComponentContext

/**
 * Task 模組裡的「列表」頁。
 * - 「+」FAB / 點 task 卡片 → 觸發 chrome action (LocalAppActions)，跟 TaskComponent 無關
 * - 「返回」(列表是模組起點，按返回離開整個 Task 模組) → onClose
 */
interface TaskListComponent : ComponentContext {
    fun onClose()
}

class DefaultTaskListComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
) : TaskListComponent, ComponentContext by componentContext {

    override fun onClose() = onFinished()
}
