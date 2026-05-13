package com.pollyannawu.justwoo.android.ui.home

import androidx.compose.ui.graphics.Color
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.core.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Encodes the rule written as a sticky-note in the Figma file:
 *   Red    = Task due today (regardless of hours remaining)
 *   Yellow = Task due tomorrow
 *   Green  = Task due day-after-tomorrow or later (shown as date)
 */
enum class TaskUrgency { Red, Yellow, Green, Past }

data class UrgencyPalette(val accent: Color, val background: Color)

fun Task.urgency(now: kotlinx.datetime.Instant = Clock.System.now(), zone: TimeZone = TimeZone.currentSystemDefault()): TaskUrgency {
    val today: LocalDate = now.toLocalDateTime(zone).date
    val due: LocalDate = this.dueTime.toLocalDateTime(zone).date
    val diff = today.daysUntil(due)
    return when {
        diff < 0 -> TaskUrgency.Past
        diff == 0 -> TaskUrgency.Red
        diff == 1 -> TaskUrgency.Yellow
        else -> TaskUrgency.Green
    }
}

fun TaskUrgency.palette(): UrgencyPalette = when (this) {
    TaskUrgency.Red -> UrgencyPalette(JustWooColors.UrgencyRed, JustWooColors.UrgencyRedBg)
    TaskUrgency.Yellow -> UrgencyPalette(JustWooColors.UrgencyYellow, JustWooColors.UrgencyYellowBg)
    TaskUrgency.Green -> UrgencyPalette(JustWooColors.UrgencyGreen, JustWooColors.UrgencyGreenBg)
    TaskUrgency.Past -> UrgencyPalette(JustWooColors.TextSecondary, JustWooColors.Outline)
}

fun TaskUrgency.label(dueDate: LocalDate): String = when (this) {
    TaskUrgency.Red -> "Today"
    TaskUrgency.Yellow -> "Tomorrow"
    TaskUrgency.Green -> "${dueDate.monthNumber}/${dueDate.dayOfMonth}"
    TaskUrgency.Past -> "Overdue"
}
