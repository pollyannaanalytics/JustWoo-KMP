package com.pollyannawu.justwoo.backend.di

import com.pollyannawu.justwoo.backend.repositories.DefaultHouseRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultProfileRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultTaskRepository
import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.repositories.TaskRepository
import com.pollyannawu.justwoo.backend.repositories.auth.AuthRepository
import com.pollyannawu.justwoo.backend.repositories.auth.DefaultAuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf<HouseRepository>(::DefaultHouseRepository)
    singleOf<TaskRepository>(::DefaultTaskRepository)
    singleOf<ProfileRepository>(::DefaultProfileRepository)
    singleOf<AuthRepository>(::DefaultAuthRepository)

}
