package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.data.HouseInviteRepository

class CreateEmailInvitationUseCase(
    private val houseInviteRepository: HouseInviteRepository,
) {
    suspend operator fun invoke(houseId: Long, email: String): EmailInvitationResponse =
        houseInviteRepository.createEmailInvitation(houseId, email)
}
