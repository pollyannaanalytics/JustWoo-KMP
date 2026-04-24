package com.pollyannawu.justwoo.android.ui.calendar

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.home.TaskCard
import com.pollyannawu.justwoo.android.ui.home.urgency
import com.pollyannawu.justwoo.android.ui.home.palette
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.core.Task
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun CalendarScreen(
    onClose: () -> Unit,
    onOpenTask: (Task) -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    var viewMonth by remember { mutableStateOf(LocalDate(state.selectedDate.year, state.selectedDate.monthNumber, 1)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
            }
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        MonthHeader(
            month = viewMonth,
            onPrev = { viewMonth = viewMonth.plus(DatePeriod(months = -1)) },
            onNext = { viewMonth = viewMonth.plus(DatePeriod(months = 1)) }
        )

        MonthGrid(
            month = viewMonth,
            selected = state.selectedDate,
            onSelect = viewModel::selectDate,
            tasks = tasks,
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = "${state.selectedDate}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(8.dp))

        val tasksOnDate = viewModel.tasksOnSelectedDate(tasks)
        if (tasksOnDate.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nothing scheduled on this day.",
                    color = JustWooColors.TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasksOnDate, key = { it.id }) { TaskCard(task = it, onClick = onOpenTask) }
            }
        }
    }
}

@Composable
private fun MonthHeader(month: LocalDate, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month") }
        Text(
            text = "${month.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${month.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = "Next month") }
    }
}

@Composable
private fun MonthGrid(
    month: LocalDate,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    tasks: List<Task>,
) {
    val firstOfMonth = LocalDate(month.year, month.monthNumber, 1)
    val daysInMonth = when (month.monthNumber) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (month.year % 4 == 0 && (month.year % 100 != 0 || month.year % 400 == 0)) 29 else 28
        else -> 30
    }
    val firstDow = firstOfMonth.dayOfWeek.ordinal // Monday = 0
    val cells = buildList {
        repeat(firstDow) { add(null) }
        repeat(daysInMonth) { add(firstOfMonth.plus(DatePeriod(days = it))) }
    }
    val tz = TimeZone.currentSystemDefault()
    val dotsByDate = tasks.groupBy { it.dueTime.toLocalDateTime(tz).date }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, color = JustWooColors.TextSecondary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(260.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(cells.size) { idx ->
                val date = cells[idx]
                if (date == null) {
                    Box(Modifier.size(36.dp))
                } else {
                    val isSelected = date == selected
                    val dayTasks = dotsByDate[date].orEmpty()
                    val accent = dayTasks.firstOrNull()?.urgency()?.palette()?.accent
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) JustWooColors.Primary else JustWooColors.CreamSurface)
                            .clickable { onSelect(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isSelected) JustWooColors.OnPrimary else JustWooColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (accent != null) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(accent)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
