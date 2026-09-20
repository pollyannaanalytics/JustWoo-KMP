package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.OtpInviteSession
import com.pollyannawu.justwoo.data.HouseInviteRepository

class GenerateOtpSessionUseCase(private val repository: HouseInviteRepository) {
    suspend operator fun invoke(): OtpInviteSession = repository.generateOtpSession()
}
