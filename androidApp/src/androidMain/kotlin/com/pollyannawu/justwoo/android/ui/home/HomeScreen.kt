package com.pollyannawu.justwoo.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.core.Task
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    currentUserId: Long,
    currentHouseId: Long,
    onCreateTask: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val filteredFlow = remember(currentUserId, state.filter) { viewModel.filteredTasks(currentUserId) }
    val tasks by filteredFlow.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = JustWooColors.Cream,
        topBar = { HomeTopBar(onOpenCalendar = onOpenCalendar, onOpenProfile = onOpenProfile) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTask,
                containerColor = JustWooColors.Primary,
                contentColor = JustWooColors.OnPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            FilterChips(
                selected = state.filter,
                onSelect = viewModel::setFilter,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tasks yet — tap + to create one.",
                        color = JustWooColors.TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(task = task, onClick = onOpenTask)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onOpenCalendar: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(JustWooColors.Cream)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "JUST WOO",
            color = JustWooColors.TextPrimary,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onOpenCalendar) {
            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = JustWooColors.TextPrimary)
        }
        IconButton(onClick = onOpenProfile) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = JustWooColors.TextPrimary)
        }
    }
}

@Composable
private fun FilterChips(
    selected: HomeViewModel.Filter,
    onSelect: (HomeViewModel.Filter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeViewModel.Filter.values().forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) JustWooColors.Primary else JustWooColors.CreamSurface)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter.name,
                    color = if (isSelected) JustWooColors.OnPrimary else JustWooColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
