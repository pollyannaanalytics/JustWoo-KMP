package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.data.HouseInviteRepository

class SubmitJoinRequestUseCase(
    private val repository: HouseInviteRepository,
) {
    suspend operator fun invoke(inviteCode: String): JoinRequestResponse =
        repository.submitJoinRequest(inviteCode)
}
