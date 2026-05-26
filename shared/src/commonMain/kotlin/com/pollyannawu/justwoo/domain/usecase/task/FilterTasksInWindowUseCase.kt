package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Pure date-window filter: keeps tasks whose `dueTime` falls within
 * `[anchor - daysBefore, anchor + daysAfter]` (both inclusive). Sorted by `dueTime` ascending.
 *
 * No repository / coroutines / status / assignee logic here — composed by
 * `ObserveHomeTodayTasksUseCase` / `ObserveProfileTasksInWindowUseCase`.
 */
class FilterTasksInWindowUseCase(
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
    operator fun invoke(
        tasks: List<Task>,
        daysBefore: Int,
        daysAfter: Int,
        anchor: LocalDate = clock.now().toLocalDateTime(zone).date,
    ): List<Task> {
        require(daysBefore >= 0 && daysAfter >= 0) { "window days must be non-negative" }
        val start = anchor.minus(daysBefore, DateTimeUnit.DAY)
        val end = anchor.plus(daysAfter, DateTimeUnit.DAY)
        return tasks
            .filter { task ->
                val dueDate = task.dueTime.toLocalDateTime(zone).date
                dueDate in start..end
            }
            .sortedBy { it.dueTime }
    }
}
