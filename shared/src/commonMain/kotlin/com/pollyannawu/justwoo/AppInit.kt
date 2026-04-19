package com.pollyannawu.justwoo

import io.ktor.util.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

fun initApp(
    platformLogger: Logger,
    platformModule: Module,
){
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


}