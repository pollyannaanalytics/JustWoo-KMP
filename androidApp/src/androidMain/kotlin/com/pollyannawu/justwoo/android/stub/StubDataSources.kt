package com.pollyannawu.justwoo.android.stub

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.dto.User
import com.pollyannawu.justwoo.datasource.AuthDataSource
import com.pollyannawu.justwoo.datasource.HouseDataSource
import com.pollyannawu.justwoo.datasource.TaskDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory stubs so the Android module can exercise the repository/ViewModel
 * layer without a real backend. The real KMP-side implementations (Ktor for
 * API, SQLDelight for local persistence) should live in :shared.
 */

class StubAuthDataSource(
    private val fixedDeviceId: String = "android-stub-device",
) : AuthDataSource {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var user: User? = null

    override fun getToken(): String? = accessToken
    override fun getUser(): User? = user
    override fun getDeviceId(): String = fixedDeviceId

    override suspend fun saveAccessToken(token: String) { accessToken = token }
    override suspend fun saveRefreshToken(token: String) { refreshToken = token }
    override suspend fun saveUser(user: User) { this.user = user }
    override suspend fun removeToken() { accessToken = null; refreshToken = null }
    override suspend fun clearAll() { accessToken = null; refreshToken = null; user = null }
}

class StubHouseDataSource : HouseDataSource {
    private val housesFlow = MutableStateFlow<List<House>>(emptyList())

    override fun getHouses(): Flow<List<House>> = housesFlow.asStateFlow()

    override suspend fun getHouseById(id: Long): House =
        housesFlow.value.first { it.id == id }

    override suspend fun createHouse(house: House) {
        housesFlow.value = housesFlow.value + house
    }

    override suspend fun updateHouse(house: House) {
        housesFlow.value = housesFlow.value.map { if (it.id == house.id) house else it }
    }

    override suspend fun updateHouses(houses: List<House>) {
        housesFlow.value = houses
    }

    override suspend fun deleteHouse(id: Long) {
        housesFlow.value = housesFlow.value.filterNot { it.id == id }
    }
}

class StubTaskDataSource : TaskDataSource {
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    override fun getTasks(): Flow<List<Task>> = tasksFlow.asStateFlow()

    override suspend fun getTaskById(id: Long): Task? =
        tasksFlow.value.firstOrNull { it.id == id }

    override suspend fun saveTask(task: Task) {
        tasksFlow.value = tasksFlow.value + task
    }

    override suspend fun updateTask(task: Task) {
        tasksFlow.value = tasksFlow.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun updateTasks(tasks: List<Task>) {
        tasksFlow.value = tasks
    }

    override suspend fun deleteTask(id: Long) {
        tasksFlow.value = tasksFlow.value.filterNot { it.id == id }
    }
}
