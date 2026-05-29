package com.pollyannawu.justwoo.android.ui.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.pollyannawu.justwoo.android.ui.home.components.HomeBottomBar
import com.pollyannawu.justwoo.android.ui.home.components.HomeMenuSheet
import com.pollyannawu.justwoo.android.ui.home.components.HomeTopBar
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors

/**
 * Persistent chrome (top bar + bottom bar + menu sheet) shared across all
 * "main" screens (Home, Tasks, …). Each screen renders content-only inside
 * the [content] slot — it receives [PaddingValues] from the Scaffold so it
 * can apply the correct insets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    content: @Composable (PaddingValues) -> Unit,
) {
    val actions = LocalAppActions.current
    var showMenu by rememberSaveable { mutableStateOf(false) }

    if (showMenu) {
        HomeMenuSheet(
            onDismiss = { showMenu = false },
            onHouseInfo = { showMenu = false; actions.onHouseInfoClick() },
            onProfileEdit = { showMenu = false; actions.onProfileClick() },
            onLeaveHouse = { showMenu = false; actions.onLeaveHouse() },
            onLogOut = { showMenu = false; actions.onLogOut() },
        )
    }

    Scaffold(
        containerColor = JustWooColors.Cream,
        topBar = {
            HomeTopBar(onOpenProfile = actions.onProfileClick)
        },
        bottomBar = {
            HomeBottomBar(
                onOpenMenu = { showMenu = true },
                onOpenHome = actions.onHomeClick,
                onCreateTask = actions.onCreateTaskClick,
                onOpenTaskSpace = actions.onTaskListClick,
                onOpenSettlements = { /* TODO: settlements 還沒建 */ },
            )
        },
        content = content,
    )
}
