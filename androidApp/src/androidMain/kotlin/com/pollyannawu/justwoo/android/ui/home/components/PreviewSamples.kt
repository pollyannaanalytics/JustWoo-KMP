package com.pollyannawu.justwoo.android.ui.home.components

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

internal object PreviewSamples {
    private val zone = TimeZone.currentSystemDefault()
    private val today = Clock.System.now().toLocalDateTime(zone).date

    fun task(
        id: Long = 1L,
        title: String = "買電池",
        hour: Int = 15,
        minute: Int = 0,
        daysOffset: Int = 0,
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
        dueTime = today.plus(daysOffset, DateTimeUnit.DAY).atStartOfDayIn(zone)
            .plus(hour, DateTimeUnit.HOUR)
            .plus(minute, DateTimeUnit.MINUTE),
        createTime = Clock.System.now(),
        updateTime = Clock.System.now(),
    )

    val todayTasks: List<Task> = listOf(
        task(id = 1L, title = "倒垃圾", hour = 9, minute = 0, daysOffset = -1),
        task(id = 2L, title = "繳水電費", hour = 14, minute = 0, daysOffset = -2),
        task(id = 3L, title = "買電池", hour = 15, minute = 0, daysOffset = 0),
        task(id = 4L, title = "帶豆漿去散步", hour = 18, minute = 0, daysOffset = 0),
        task(id = 5L, title = "訂早餐", hour = 8, minute = 30, daysOffset = 1),
        task(id = 6L, title = "打電話給媽媽", hour = 20, minute = 0, daysOffset = 1),
    )
}
