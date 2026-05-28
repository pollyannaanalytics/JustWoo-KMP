package com.pollyannawu.justwoo.android.di

import com.pollyannawu.justwoo.android.ui.MainViewModel
import com.pollyannawu.justwoo.android.ui.auth.RegisterViewModel
import com.pollyannawu.justwoo.android.ui.auth.SignInViewModel
import com.pollyannawu.justwoo.android.ui.home.HomeViewModel
import com.pollyannawu.justwoo.android.ui.house.CreateHouseViewModel
import com.pollyannawu.justwoo.android.ui.house.GenerateInviteCodeViewModel
import com.pollyannawu.justwoo.android.ui.house.JoinHouseViewModel
import com.pollyannawu.justwoo.android.ui.house.PendingRequestsViewModel
import com.pollyannawu.justwoo.android.ui.profile.ProfileEditViewModel
import com.pollyannawu.justwoo.android.ui.task.CreateTaskViewModel
import com.pollyannawu.justwoo.android.ui.task.TaskExplorationViewModel
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.data.DefaultAuthRepository
import com.pollyannawu.justwoo.data.DefaultHouseInviteRepository
import com.pollyannawu.justwoo.data.DefaultHouseRepository
import com.pollyannawu.justwoo.data.DefaultTaskRepository
import com.pollyannawu.justwoo.data.HouseInviteRepository
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.TaskRepository
import com.pollyannawu.justwoo.domain.usecase.auth.HasActiveSessionUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.HasOnboardedUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.LoginUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.LogoutUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveIsAuthenticatedUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.RegisterUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ApproveMemberUseCase
import com.pollyannawu.justwoo.domain.usecase.house.CreateHouseUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GenerateInviteCodeUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetJoinRequestStatusUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ObserveHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.house.RejectMemberUseCase
import com.pollyannawu.justwoo.domain.usecase.house.SubmitJoinRequestUseCase
import com.pollyannawu.justwoo.domain.usecase.task.CreateTaskUseCase
import com.pollyannawu.justwoo.domain.usecase.task.FilterPendingTasksForUserUseCase
import com.pollyannawu.justwoo.domain.usecase.task.FilterTasksInWindowUseCase
import com.pollyannawu.justwoo.domain.usecase.task.GetHomeTodayTasksUseCase
import com.pollyannawu.justwoo.domain.usecase.task.GetPendingTasksForUserUseCase
import com.pollyannawu.justwoo.domain.usecase.task.GetProfileTasksInWindowUseCase
import com.pollyannawu.justwoo.domain.usecase.task.ObserveAllTasksUseCase
import com.pollyannawu.justwoo.domain.usecase.task.ObserveHomeTodayTasksUseCase
import com.pollyannawu.justwoo.domain.usecase.task.ObservePendingTasksForUserUseCase
import com.pollyannawu.justwoo.domain.usecase.task.ObserveProfileTasksInWindowUseCase
import com.pollyannawu.justwoo.domain.usecase.task.SubmitTaskDecisionUseCase
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
    single<HouseInviteRepository> {
        DefaultHouseInviteRepository(
            houseInviteApiService = get(),
            houseRepository = get(),
        )
    }

    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ObserveCurrentUserIdUseCase(get()) }
    factory { ObserveCurrentHouseIdUseCase(get()) }
    factory { ObserveIsAuthenticatedUseCase(get()) }
    factory { HasActiveSessionUseCase(get()) }
    factory { HasOnboardedUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { FilterTasksInWindowUseCase() }
    factory { ObserveAllTasksUseCase(get()) }
    factory { ObserveHomeTodayTasksUseCase(get(), get()) }
    factory { GetHomeTodayTasksUseCase(get(), get()) }
    factory { ObserveProfileTasksInWindowUseCase(get(), get()) }
    factory { GetProfileTasksInWindowUseCase(get(), get()) }
    factory { FilterPendingTasksForUserUseCase() }
    factory { ObservePendingTasksForUserUseCase(get(), get()) }
    factory { GetPendingTasksForUserUseCase(get(), get()) }
    factory { SubmitTaskDecisionUseCase(get()) }
    factory { CreateTaskUseCase(get()) }
    factory { ObserveHouseMembersUseCase(get()) }
    factory { GetHouseMembersUseCase(get()) }
    factory { CreateHouseUseCase(get()) }
    factory { GenerateInviteCodeUseCase(get()) }
    factory { SubmitJoinRequestUseCase(get()) }
    factory { ApproveMemberUseCase(get()) }
    factory { RejectMemberUseCase(get()) }
    factory { GetJoinRequestStatusUseCase(get()) }

    viewModel { MainViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { CreateTaskViewModel(get(), get()) } // observeHouseMembers, createTask
    viewModel { TaskExplorationViewModel(get(), get()) }
    viewModel { ProfileEditViewModel(get(), get()) }
    viewModel { CreateHouseViewModel(get()) }
    viewModel { JoinHouseViewModel(get(), get()) }
    viewModel { GenerateInviteCodeViewModel(get(), get(), get(), get()) }
    viewModel { PendingRequestsViewModel(get(), get(), get(), get()) }
}
