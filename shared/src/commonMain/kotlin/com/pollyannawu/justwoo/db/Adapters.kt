package com.pollyannawu.justwoo.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.TaskStatus
import kotlinx.datetime.Instant

object InstantAdapter : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}

val accessLevelAdapter: ColumnAdapter<AccessLevel, String> = EnumColumnAdapter()
val taskStatusAdapter: ColumnAdapter<TaskStatus, String> = EnumColumnAdapter()
val assignStatusAdapter: ColumnAdapter<AssignStatus, String> = EnumColumnAdapter()
val memberRoleAdapter: ColumnAdapter<MemberRole, String> = EnumColumnAdapter()
