package com.pollyannawu.justwoo.android.ui.home.components

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Sample data for @Preview functions in this package. Keep here, not under
 * `previewParameters/`, so previews don't depend on resources.
 */
internal object PreviewSamples {
    private val zone = TimeZone.currentSystemDefault()
    private val today = Clock.System.now().toLocalDateTime(zone).date

    fun task(
        id: Long = 1L,
        title: String = "買電池",
        hour: Int = 15,
        minute: Int = 0,
        status: TaskStatus = TaskStatus.TODO,
    ): Task = Task(
        id = id,
        title = title,
        description = "",
        accessLevel = AccessLevel.PUBLIC,
        taskStatus = status,
        ownerId = 1L,
        houseId = 1L,
        assignees = emptyList(),
        dueTime = today.atStartOfDayIn(zone)
            .plus(hour, DateTimeUnit.HOUR)
            .plus(minute, DateTimeUnit.MINUTE),
        createTime = Clock.System.now(),
        updateTime = Clock.System.now(),
    )

    val todayTasks: List<Task> = listOf(
        task(id = 1L, title = "買電池", hour = 15, minute = 0),
        task(id = 2L, title = "帶豆漿去散步", hour = 18, minute = 0),
        task(id = 3L, title = "倒垃圾", hour = 21, minute = 30),
    )
}
