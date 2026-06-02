package com.pollyannawu.justwoo.domain.usecase.profile

import com.pollyannawu.justwoo.data.ProfileRepository

class UpdateCurrentProfileUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(name: String, bio: String, bankAccount: String) =
        profileRepository.updateCurrentProfile(name, bio, bankAccount)
}
