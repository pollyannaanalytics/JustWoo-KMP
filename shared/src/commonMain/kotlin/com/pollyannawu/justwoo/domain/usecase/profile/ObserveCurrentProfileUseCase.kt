package com.pollyannawu.justwoo.domain.usecase.profile

import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.data.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentProfileUseCase(
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(): Flow<Profile?> = profileRepository.observeCurrentProfile()
}
