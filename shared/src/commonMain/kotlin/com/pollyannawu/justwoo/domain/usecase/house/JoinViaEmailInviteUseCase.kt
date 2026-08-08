package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.data.HouseInviteRepository

/**
 * Submits an invite code for the email-invitation auto-approve join path.
 *
 * Delegates to the same repository method (and backend route) as the generic
 * invite-code join flow ([SubmitJoinRequestUseCase]). Kept as a distinct
 * use case purely for call-site clarity: when the code matches a pending
 * email invitation, the backend auto-approves and returns a
 * [JoinRequestResponse] with `status == APPROVED` (where `id` is the email
 * invitation's id, not a pollable join-request id). If the code doesn't
 * match any email invitation, the backend falls through to the existing
 * generic invite-code path unchanged.
 */
class JoinViaEmailInviteUseCase(
    private val houseInviteRepository: HouseInviteRepository,
) {
    suspend operator fun invoke(inviteCode: String): JoinRequestResponse =
        houseInviteRepository.submitJoinRequest(inviteCode)
}
