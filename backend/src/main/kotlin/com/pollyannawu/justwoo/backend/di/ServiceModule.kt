package com.pollyannawu.justwoo.backend.di

import com.pollyannawu.justwoo.backend.service.AuthService
import com.pollyannawu.justwoo.backend.service.DefaultAuthService
import com.pollyannawu.justwoo.backend.service.DefaultHouseService
import com.pollyannawu.justwoo.backend.service.DefaultTaskService
import com.pollyannawu.justwoo.backend.service.HouseService
import com.pollyannawu.justwoo.backend.service.TaskService
import com.pollyannawu.justwoo.backend.utils.security.BcryptHashPasswordProvider
import com.pollyannawu.justwoo.backend.utils.security.HashPasswordProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val servicesModule = module {
    single<HouseService>{ DefaultHouseService(get(), get()) }
    single<TaskService>{ DefaultTaskService(get(), get(), get()) }
    single<AuthService> { DefaultAuthService(get(), get(), get(), get(), get()) }
    singleOf<HashPasswordProvider>(::BcryptHashPasswordProvider)
}