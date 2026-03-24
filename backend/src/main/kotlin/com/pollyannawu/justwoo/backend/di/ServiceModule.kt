package com.pollyannawu.justwoo.backend.di

import com.pollyannawu.justwoo.backend.service.DefaultHouseService
import com.pollyannawu.justwoo.backend.service.DefaultTaskService
import com.pollyannawu.justwoo.backend.utils.security.JwtService
import com.pollyannawu.justwoo.backend.utils.security.SecurityService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val servicesModule = module {
    singleOf(::DefaultHouseService)
    singleOf(::DefaultTaskService)
    single<SecurityService> { JwtService(get()) }
}