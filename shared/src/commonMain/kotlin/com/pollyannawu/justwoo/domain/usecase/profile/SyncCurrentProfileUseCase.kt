package com.pollyannawu.justwoo.domain.usecase.profile

import com.pollyannawu.justwoo.data.ProfileRepository

class SyncCurrentProfileUseCase(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke() = profileRepository.syncCurrentProfile()
}
