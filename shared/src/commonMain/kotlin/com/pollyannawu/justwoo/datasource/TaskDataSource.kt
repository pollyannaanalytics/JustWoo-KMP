package com.pollyannawu.justwoo.datasource

import com.pollyannawu.justwoo.core.Task
import kotlinx.coroutines.flow.Flow

interface TaskDataSource {
    fun getTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun saveTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun updateTasks(tasks: List<Task>)
    suspend fun deleteTask(id: Long)
}
