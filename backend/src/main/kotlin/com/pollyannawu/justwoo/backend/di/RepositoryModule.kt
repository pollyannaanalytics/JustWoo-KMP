package com.pollyannawu.justwoo.backend.di

import com.pollyannawu.justwoo.backend.repositories.DefaultEmailInvitationRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultHouseRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultInviteCodeRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultJoinRequestRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultProfileRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultSettlementRepository
import com.pollyannawu.justwoo.backend.repositories.DefaultTaskRepository
import com.pollyannawu.justwoo.backend.repositories.EmailInvitationRepository
import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.InviteCodeRepository
import com.pollyannawu.justwoo.backend.repositories.JoinRequestRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.repositories.SettlementRepository
import com.pollyannawu.justwoo.backend.repositories.TaskRepository
import com.pollyannawu.justwoo.backend.repositories.auth.AuthRepository
import com.pollyannawu.justwoo.backend.repositories.auth.DefaultAuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf<HouseRepository>(::DefaultHouseRepository)
    singleOf<TaskRepository>(::DefaultTaskRepository)
    singleOf<ProfileRepository>(::DefaultProfileRepository)
    singleOf<AuthRepository>(::DefaultAuthRepository)
    singleOf<SettlementRepository>(::DefaultSettlementRepository)
    singleOf<InviteCodeRepository>(::DefaultInviteCodeRepository)
    singleOf<JoinRequestRepository>(::DefaultJoinRequestRepository)
    singleOf<EmailInvitationRepository>(::DefaultEmailInvitationRepository)
}
