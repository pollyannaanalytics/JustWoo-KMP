package com.pollyannawu.justwoo.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.home.TaskCard
import com.pollyannawu.justwoo.android.ui.home.palette
import com.pollyannawu.justwoo.android.ui.home.urgency
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.Task
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileEditScreen(
    onClose: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val tasksInWindow by viewModel.tasksInWindow.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onClose()
        }
    }

    ProfileEditContent(
        state = state,
        allTasks = allTasks,
        tasksInWindow = tasksInWindow,
        onClose = onClose,
        onNameChange = viewModel::onNameChange,
        onBioChange = viewModel::onBioChange,
        onNewHashtagChange = viewModel::onNewHashtagChange,
        onAddHashtag = viewModel::addHashtag,
        onRemoveHashtag = viewModel::removeHashtag,
        onSelectDate = viewModel::selectDate,
        onSave = viewModel::save,
    )
}

@Composable
private fun ProfileEditContent(
    state: ProfileEditViewModel.UiState,
    allTasks: List<Task>,
    tasksInWindow: List<Task>,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onNewHashtagChange: (String) -> Unit,
    onAddHashtag: () -> Unit,
    onRemoveHashtag: (String) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onSave: () -> Unit,
) {
    var viewMonth by remember {
        mutableStateOf(LocalDate(state.selectedDate.year, state.selectedDate.monthNumber, 1))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = JustWooSpacing.XSmall, vertical = JustWooSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = JustWooColors.TextPrimary,
                )
            }
            Text(
                text = "Edit profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = JustWooFontWeight.Bold,
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = JustWooSpacing.XLarge, vertical = JustWooSpacing.Small)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = JustWooSpacing.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(JustWooColors.UrgencyYellowBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.name.take(1).uppercase().ifBlank { "W" },
                            fontWeight = JustWooFontWeight.Black,
                            style = MaterialTheme.typography.displayMedium,
                            color = JustWooColors.PrimaryDeep,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(JustWooSpacing.XLarge)) }

            item { Text("Name", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                JustWooTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    placeholder = "Your name",
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { CounterHint(state.name.length, ProfileEditViewModel.NAME_LIMIT) }
            item { Spacer(Modifier.height(JustWooSpacing.Default)) }

            item { Text("Bio", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                JustWooTextField(
                    value = state.bio,
                    onValueChange = onBioChange,
                    placeholder = "A few words about you",
                    isError = state.bioError != null,
                    errorMessage = state.bioError,
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
            item { CounterHint(state.bio.length, ProfileEditViewModel.BIO_LIMIT) }
            item { Spacer(Modifier.height(JustWooSpacing.Default)) }

            item { Text("Hashtags", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JustWooTextField(
                        value = state.newHashtag,
                        onValueChange = onNewHashtagChange,
                        placeholder = "add #hashtag",
                        isError = state.hashtagError != null,
                        errorMessage = state.hashtagError,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(JustWooSpacing.Small))
                    Box(
                        modifier = Modifier
                            .clip(JustWooShapes.XLarge)
                            .background(JustWooColors.Primary)
                            .clickable { onAddHashtag() }
                            .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Medium)
                    ) {
                        Text("Add", color = JustWooColors.OnPrimary, fontWeight = JustWooFontWeight.Bold)
                    }
                }
            }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Small)) {
                    items(state.hashtags, key = { it }) { tag ->
                        HashtagChip(tag = tag, onRemove = { onRemoveHashtag(tag) })
                    }
                }
            }

            item { Spacer(Modifier.height(JustWooSpacing.XLarge)) }
            item {
                Text("Calendar", fontWeight = JustWooFontWeight.SemiBold)
            }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }

            item {
                MonthHeader(
                    month = viewMonth,
                    onPrev = { viewMonth = viewMonth.plus(DatePeriod(months = -1)) },
                    onNext = { viewMonth = viewMonth.plus(DatePeriod(months = 1)) }
                )
            }

            item {
                MonthGrid(
                    month = viewMonth,
                    selected = state.selectedDate,
                    onSelect = onSelectDate,
                    tasks = allTasks,
                )
            }

            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                Text(
                    text = "${state.selectedDate}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = JustWooFontWeight.Bold,
                )
            }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }

            if (tasksInWindow.isEmpty()) {
                item {
                    Text(
                        "No tasks in this window.",
                        color = JustWooColors.TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(tasksInWindow, key = { it.id }) { task ->
                    TaskCard(task = task, onClick = {})
                    Spacer(Modifier.height(JustWooSpacing.Medium))
                }
            }

            item { Spacer(Modifier.height(JustWooSpacing.Large)) }
            item {
                JustWooPrimaryButton(
                    text = "Save",
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Spacer(Modifier.height(JustWooSpacing.Large)) }
        }
    }
}

@Composable
private fun MonthHeader(month: LocalDate, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = JustWooSpacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month") }
        Text(
            text = "${month.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${month.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = JustWooFontWeight.Bold,
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
                        contentAlignment = Alignment.Center
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

@Composable
private fun CounterHint(current: Int, max: Int) {
    Text(
        text = "$current/$max",
        color = if (current > max) JustWooColors.Error else JustWooColors.TextSecondary,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = JustWooSpacing.Medium, top = 2.dp),
    )
}

@Composable
private fun HashtagChip(tag: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(JustWooShapes.Large)
            .background(JustWooColors.UrgencyYellowBg)
            .padding(horizontal = JustWooSpacing.Medium, vertical = JustWooSpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#$tag", color = JustWooColors.PrimaryDeep, fontWeight = JustWooFontWeight.SemiBold)
        Spacer(Modifier.width(JustWooSpacing.XSmall))
        Icon(
            Icons.Default.Close,
            contentDescription = "Remove",
            tint = JustWooColors.PrimaryDeep,
            modifier = Modifier.size(16.dp).clickable(onClick = onRemove)
        )
    }
}
