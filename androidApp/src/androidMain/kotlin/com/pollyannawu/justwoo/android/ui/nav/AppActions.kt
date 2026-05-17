package com.pollyannawu.justwoo.android.ui.nav

import androidx.compose.runtime.staticCompositionLocalOf

data class AppActions(
    val onProfileClick: () -> Unit,
    val onCreateTaskClick: () -> Unit,
    val onTaskListClick: () -> Unit,
    val onTaskQuickClick: (taskId: Long) -> Unit,
)

val LocalAppActions = staticCompositionLocalOf<AppActions> {
    error("AppActions not provided. Wrap your UI with CompositionLocalProvider in RootContent.")
}
