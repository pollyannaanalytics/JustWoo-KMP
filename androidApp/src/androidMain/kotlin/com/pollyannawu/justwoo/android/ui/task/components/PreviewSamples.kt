package com.pollyannawu.justwoo.android.ui.task.components

import com.pollyannawu.justwoo.android.ui.task.CreateTaskViewModel
import com.pollyannawu.justwoo.core.AccessLevel
import kotlinx.datetime.Clock

internal object PreviewSamples {
    val assignees = listOf(
        CreateTaskViewModel.Assignee(id = 1L, label = "Alice"),
        CreateTaskViewModel.Assignee(id = 2L, label = "Bob"),
    )

    val uiState = CreateTaskViewModel.UiState(
        title = "Buy groceries",
        accessLevel = AccessLevel.PUBLIC,
        availableAssignees = assignees,
        dueTime = Clock.System.now(),
    )
}
