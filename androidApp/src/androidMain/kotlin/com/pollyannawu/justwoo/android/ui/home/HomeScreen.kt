package com.pollyannawu.justwoo.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pollyannawu.justwoo.android.ui.home.components.MonthlyStatCard
import com.pollyannawu.justwoo.android.ui.home.components.PreviewSamples
import com.pollyannawu.justwoo.android.ui.home.components.TaskSpaceCard
import com.pollyannawu.justwoo.android.ui.home.components.TodayToDoCard
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.Task
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUserId: Long,
    currentHouseId: Long,
    padding: PaddingValues,
    onOpenTaskSpace: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val todayTasks by remember(currentUserId) { viewModel.todayTasks(currentUserId) }
        .collectAsState(initial = emptyList())
    val monthlyCount by remember(currentUserId) { viewModel.monthlyCount(currentUserId) }
        .collectAsState(initial = 0)
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState.refreshing,
        onRefresh = { viewModel.refresh(currentHouseId) },
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

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 680)
@Composable
private fun HomeScreenPreview() {
    JustWooTheme {
        HomeScreen(
            currentUserId = 1L,
            currentHouseId = 1L,
            padding = PaddingValues(),
            onOpenTaskSpace = {},
        )
    }
}
