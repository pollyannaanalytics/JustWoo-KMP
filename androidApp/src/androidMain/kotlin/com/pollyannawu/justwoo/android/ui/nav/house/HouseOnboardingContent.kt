package com.pollyannawu.justwoo.android.ui.nav.house

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.pollyannawu.justwoo.android.ui.house.ConfirmOtpInviteScreen
import com.pollyannawu.justwoo.android.ui.house.CreateHouseScreen
import com.pollyannawu.justwoo.android.ui.house.HouseOnboardingScreen
import com.pollyannawu.justwoo.android.ui.house.JoinHouseScreen
import com.pollyannawu.justwoo.android.ui.house.OtpSessionScreen
import com.pollyannawu.justwoo.android.ui.house.PendingInvitationsScreen
import com.pollyannawu.justwoo.ui.nav.house.HouseOnboardingComponent

@Composable
fun HouseOnboardingContent(component: HouseOnboardingComponent) {
    Children(
        stack = component.stack,
        animation = stackAnimation(fade() + slide()),
    ) { created ->
        when (val child = created.instance) {
            is HouseOnboardingComponent.Child.SelectAction -> HouseOnboardingScreen(
                onJoinClick = child.component::onJoinSelected,
                onCreateClick = child.component::onCreateSelected,
                onMyInvitationsClick = child.component::onMyInvitationsSelected,
                onShowMyCodeClick = child.component::onShowMyCodeSelected,
                onConfirmInviteCodeClick = child.component::onConfirmOtpInviteSelected,
            )
            is HouseOnboardingComponent.Child.JoinHouse -> JoinHouseScreen(
                component = child.component,
            )
            is HouseOnboardingComponent.Child.CreateHouse -> CreateHouseScreen(
                component = child.component,
            )
            is HouseOnboardingComponent.Child.PendingInvitations -> PendingInvitationsScreen(
                componentContext = child.component,
                onBack = child.component::onBack,
                onJoinSuccess = child.component::onCompleted,
            )
            is HouseOnboardingComponent.Child.OtpSession -> OtpSessionScreen(
                component = child.component,
            )
            is HouseOnboardingComponent.Child.ConfirmOtpInvite -> ConfirmOtpInviteScreen(
                component = child.component,
            )
        }
    }
}
