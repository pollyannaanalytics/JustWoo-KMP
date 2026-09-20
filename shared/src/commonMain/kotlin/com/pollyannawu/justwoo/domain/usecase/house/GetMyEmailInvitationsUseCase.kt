package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.data.HouseInviteRepository

class GetMyEmailInvitationsUseCase(
    private val houseInviteRepository: HouseInviteRepository,
) {
    suspend operator fun invoke(): List<EmailInvitationResponse> =
        houseInviteRepository.getMyEmailInvitations()
}
