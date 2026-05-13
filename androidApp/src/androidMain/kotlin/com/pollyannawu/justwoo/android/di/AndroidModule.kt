package com.pollyannawu.justwoo.android.di

import com.pollyannawu.justwoo.android.session.SessionState
import com.pollyannawu.justwoo.android.stub.StubAuthApiService
import com.pollyannawu.justwoo.android.stub.StubAuthDataSource
import com.pollyannawu.justwoo.android.stub.StubHouseApiService
import com.pollyannawu.justwoo.android.stub.StubHouseDataSource
import com.pollyannawu.justwoo.android.stub.StubTaskApiService
import com.pollyannawu.justwoo.android.stub.StubTaskDataSource
import com.pollyannawu.justwoo.android.ui.auth.RegisterViewModel
import com.pollyannawu.justwoo.android.ui.auth.SignInViewModel
import com.pollyannawu.justwoo.android.ui.calendar.CalendarViewModel
import com.pollyannawu.justwoo.android.ui.home.HomeViewModel
import com.pollyannawu.justwoo.android.ui.profile.ProfileEditViewModel
import com.pollyannawu.justwoo.android.ui.task.CreateTaskViewModel
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.data.DefaultAuthRepository
import com.pollyannawu.justwoo.data.DefaultHouseRepository
import com.pollyannawu.justwoo.data.DefaultTaskRepository
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.TaskRepository
import com.pollyannawu.justwoo.datasource.AuthDataSource
import com.pollyannawu.justwoo.datasource.HouseDataSource
import com.pollyannawu.justwoo.datasource.TaskDataSource
import com.pollyannawu.justwoo.network.AuthApiService
import com.pollyannawu.justwoo.network.HouseApiService
import com.pollyannawu.justwoo.network.TaskApiService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Single Koin module for the Android app. Wires:
 *
 *   - Session state (in-memory, Android-only)
 *   - Stub API services + data sources (TODO: replace with real Ktor/SQLDelight
 *     implementations in :shared)
 *   - Shared repositories (interfaces live in :shared)
 *   - All screen ViewModels
 *
 * The UI layer only talks to repositories, so swapping the stubs for real
 * implementations later won't require any UI changes.
 */
val androidModule = module {

    // ---- Session ------------------------------------------------------------
    single { SessionState() }

    // ---- Data sources (stubs) ----------------------------------------------
    single<AuthDataSource> { StubAuthDataSource() }
    single<HouseDataSource> { StubHouseDataSource() }
    single<TaskDataSource> { StubTaskDataSource() }

    // ---- API services (stubs) ----------------------------------------------
    single<AuthApiService> { StubAuthApiService() }
    single<HouseApiService> { StubHouseApiService() }
    single<TaskApiService> { StubTaskApiService() }

    // ---- Repositories (from :shared) ---------------------------------------
    single<AuthRepository> {
        DefaultAuthRepository(
            apiService = get(),
            localDataSource = get(),
        )
    }
    single<HouseRepository> {
        DefaultHouseRepository(
            authDataSource = get(),
            houseApiService = get(),
            houseDataSource = get(),
        )
    }
    single<TaskRepository> {
        DefaultTaskRepository(
            authDataSource = get(),
            taskApiService = get(),
            taskDataSource = get(),
        )
    }

    // ---- ViewModels ---------------------------------------------------------
    viewModel { SignInViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { CreateTaskViewModel(get()) }
    viewModel { ProfileEditViewModel() }
    viewModel { CalendarViewModel(get()) }
}
