package com.pollyannawu.justwoo.di

import com.pollyannawu.justwoo.domain.usecase.house.ConfirmOtpInviteUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GenerateOtpSessionUseCase
import org.koin.dsl.module

val houseInviteModule = module {
    factory { GenerateOtpSessionUseCase(get()) }
    factory { ConfirmOtpInviteUseCase(get()) }
}
