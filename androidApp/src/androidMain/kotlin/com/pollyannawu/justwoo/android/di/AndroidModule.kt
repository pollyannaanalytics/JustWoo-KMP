package com.pollyannawu.justwoo.android.di

import com.pollyannawu.justwoo.android.ui.MainViewModel
import com.pollyannawu.justwoo.android.ui.auth.RegisterViewModel
import com.pollyannawu.justwoo.android.ui.auth.SignInViewModel
import com.pollyannawu.justwoo.android.ui.home.HomeViewModel
import com.pollyannawu.justwoo.android.ui.profile.ProfileEditViewModel
import com.pollyannawu.justwoo.android.ui.task.CreateTaskViewModel
import com.pollyannawu.justwoo.android.ui.task.TaskExplorationViewModel
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.data.DefaultAuthRepository
import com.pollyannawu.justwoo.data.DefaultHouseRepository
import com.pollyannawu.justwoo.data.DefaultTaskRepository
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.TaskRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {

    single<AuthRepository> {
        DefaultAuthRepository(
            apiService = get(),
            tokenStorage = get(),
            userStorage = get(),
            deviceIdProvider = get(),
        )
    }
    single<HouseRepository> {
        DefaultHouseRepository(
            userStorage = get(),
            houseApiService = get(),
            houseDataSource = get(),
        )
    }
    single<TaskRepository> {
        DefaultTaskRepository(
            userStorage = get(),
            taskApiService = get(),
            taskDataSource = get(),
        )
    }

    viewModel { MainViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { CreateTaskViewModel(get(), get()) }
    viewModel { TaskExplorationViewModel(get()) }
    viewModel { ProfileEditViewModel(get()) }
}
