package com.pollyannawu.justwoo.android.ui.nav.house

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.pollyannawu.justwoo.android.ui.house.CreateHouseScreen
import com.pollyannawu.justwoo.android.ui.house.HouseOnboardingScreen
import com.pollyannawu.justwoo.android.ui.house.JoinHouseScreen
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
            )
            is HouseOnboardingComponent.Child.JoinHouse -> JoinHouseScreen(
                component = child.component,
            )
            is HouseOnboardingComponent.Child.CreateHouse -> CreateHouseScreen(
                component = child.component,
            )
        }
    }
}
