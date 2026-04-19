package com.pollyannawu.justwoo.backend.database.utils

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow

@Serializable
data class PagedResult<T>(
    val items: List<T>,
    val totalCount: Long
)

fun Query.toPagedRows(
    size: Int,
    offset: Long
): PagedResult<ResultRow> {
    val totalCount = this.count()
    val rows = limit(size).offset(start = offset).toList()

    return PagedResult(rows, totalCount)
}