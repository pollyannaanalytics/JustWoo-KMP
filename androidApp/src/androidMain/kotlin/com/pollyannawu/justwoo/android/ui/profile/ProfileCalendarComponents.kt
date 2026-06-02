package com.pollyannawu.justwoo.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.home.palette
import com.pollyannawu.justwoo.android.ui.home.urgency
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.Task
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun MonthHeader(month: LocalDate, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month") }
        Text(
            text = "${month.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${month.year}",
            fontWeight = JustWooFontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = "Next month") }
    }
}

@Composable
internal fun MonthGrid(
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
    val firstDow = firstOfMonth.dayOfWeek.ordinal
    val cells = buildList {
        repeat(firstDow) { add(null) }
        repeat(daysInMonth) { add(firstOfMonth.plus(DatePeriod(days = it))) }
    }
    val tz = TimeZone.currentSystemDefault()
    val dotsByDate = tasks.groupBy { it.dueTime.toLocalDateTime(tz).date }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, color = JustWooColors.TextSecondary, fontWeight = JustWooFontWeight.Bold)
            }
        }
        Spacer(Modifier.height(JustWooSpacing.Small))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(JustWooSpacing.XSmall),
            horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.XSmall),
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
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isSelected) JustWooColors.OnPrimary else JustWooColors.TextPrimary,
                                fontWeight = JustWooFontWeight.SemiBold,
                            )
                            if (accent != null) {
                                Box(
                                    modifier = Modifier
                                        .size(JustWooSpacing.XSmall)
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
