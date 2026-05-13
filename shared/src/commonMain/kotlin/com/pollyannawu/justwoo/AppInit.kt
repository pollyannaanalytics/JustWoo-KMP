package com.pollyannawu.justwoo

import com.pollyannawu.justwoo.di.databaseModule
import com.pollyannawu.justwoo.di.networkModule
import org.koin.core.module.Module

fun sharedModules(platformModule: Module): List<Module> = listOf(
    platformModule,
    networkModule,
    databaseModule,
)
