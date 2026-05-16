package com.pollyannawu.justwoo.android.ui.nav

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.pollyannawu.justwoo.android.ui.auth.RegisterScreen
import com.pollyannawu.justwoo.android.ui.auth.SignInScreen
import com.pollyannawu.justwoo.android.ui.calendar.CalendarScreen
import com.pollyannawu.justwoo.android.ui.home.HomeScreen
import com.pollyannawu.justwoo.android.ui.profile.ProfileEditScreen
import com.pollyannawu.justwoo.android.ui.task.CreateTaskScreen
import com.pollyannawu.justwoo.android.ui.task.TaskExplorationScreen
import com.pollyannawu.justwoo.nav.RootComponent

/**
 * Renders the Decompose [RootComponent]'s stack as native Compose screens.
 * Each child's navigation callbacks come from shared/commonMain; the screens
 * themselves stay platform-native.
 */
@Composable
fun RootContent(component: RootComponent) {
    Children(
        stack = component.stack,
        animation = stackAnimation(fade() + slide()),
    ) { created ->
        when (val child = created.instance) {
            is RootComponent.Child.SignIn -> SignInScreen(
                onSignInSuccess = child.component::onSignInSuccess,
                onNavigateToRegister = child.component::onNavigateToRegister,
            )
            is RootComponent.Child.Register -> RegisterScreen(
                onRegisterSuccess = child.component::onRegisterSuccess,
                onNavigateToSignIn = child.component::onNavigateToSignIn,
            )
            is RootComponent.Child.Home -> HomeScreen(
                currentUserId = child.component.userId,
                currentHouseId = child.component.houseId,
                onCreateTask = child.component::onCreateTask,
                onOpenTaskSpace = child.component::onOpenTaskSpace,
                onOpenCalendar = child.component::onOpenCalendar,
                onOpenProfile = child.component::onOpenProfile,
                onOpenMenu = child.component::onOpenMenu,
            )
            is RootComponent.Child.CreateTask -> CreateTaskScreen(
                currentUserId = child.component.userId,
                currentHouseId = child.component.houseId,
                onClose = child.component::onClose,
                onOpenProfile = child.component::onOpenProfile,
            )
            is RootComponent.Child.TaskExploration -> TaskExplorationScreen(
                currentUserId = child.component.userId,
                currentHouseId = child.component.houseId,
                onClose = child.component::onClose,
                onOpenProfile = child.component::onOpenProfile,
            )
            is RootComponent.Child.Calendar -> CalendarScreen(
                onClose = child.component::onClose,
                onOpenTask = { /* TODO: child.component.onOpenTask(it.id) */ },
            )
            is RootComponent.Child.ProfileEdit -> ProfileEditScreen(
                onClose = child.component::onClose,
            )
        }
    }
}
