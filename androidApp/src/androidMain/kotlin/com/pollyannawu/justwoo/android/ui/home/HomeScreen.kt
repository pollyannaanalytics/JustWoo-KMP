package com.pollyannawu.justwoo.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pollyannawu.justwoo.android.ui.home.components.HomeBottomBar
import com.pollyannawu.justwoo.android.ui.home.components.HomeTopBar
import com.pollyannawu.justwoo.android.ui.home.components.MonthlyStatCard
import com.pollyannawu.justwoo.android.ui.home.components.PreviewSamples
import com.pollyannawu.justwoo.android.ui.home.components.TaskSpaceCard
import com.pollyannawu.justwoo.android.ui.home.components.TodayToDoCard
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.Task
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    currentUserId: Long,
    currentHouseId: Long,
    onCreateTask: () -> Unit,
    onOpenTaskSpace: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMenu: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val todayTasks by remember(currentUserId) { viewModel.todayTasks(currentUserId) }
        .collectAsState(initial = emptyList())
    val monthlyCount by remember(currentUserId) { viewModel.monthlyCount(currentUserId) }
        .collectAsState(initial = 0)
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        todayTasks = todayTasks,
        monthlyCount = monthlyCount,
        isRefreshing = uiState.refreshing,
        onRefresh = { viewModel.refresh(currentHouseId) },
        onCreateTask = onCreateTask,
        onOpenTaskSpace = onOpenTaskSpace,
        onOpenCalendar = onOpenCalendar,
        onOpenProfile = onOpenProfile,
        onOpenMenu = onOpenMenu,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    todayTasks: List<Task>,
    monthlyCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCreateTask: () -> Unit,
    onOpenTaskSpace: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    Scaffold(
        containerColor = JustWooColors.Cream,
        topBar = { HomeTopBar(onOpenMenu = onOpenMenu, onOpenProfile = onOpenProfile) },
        bottomBar = {
            HomeBottomBar(
                onOpenChat = { /* not in scope */ },
                onOpenHome = { /* already here */ },
                onCreateTask = onCreateTask,
                onOpenTaskSpace = onOpenTaskSpace,
                onOpenSettlements = onOpenCalendar,
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = JustWooSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Large),
            ) {
                Spacer(Modifier.height(JustWooSpacing.XSmall))
                TodayToDoCard(tasks = todayTasks)
                MonthlyStatCard(count = monthlyCount)
                TaskSpaceCard(onClick = onOpenTaskSpace)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun HomeScreenPreview() {
    JustWooTheme {
        HomeScreenContent(
            todayTasks = PreviewSamples.todayTasks,
            monthlyCount = 10,
            isRefreshing = false,
            onRefresh = {},
            onCreateTask = {},
            onOpenTaskSpace = {},
            onOpenCalendar = {},
            onOpenProfile = {},
            onOpenMenu = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    HomeScreenContent(
        todayTasks = emptyList(),
        monthlyCount = 0,
        isRefreshing = false,
        onRefresh = {},
        onCreateTask = {},
        onOpenTaskSpace = {},
        onOpenCalendar = {},
        onOpenProfile = {},
        onOpenMenu = {},
    )
}
