package com.pollyannawu.justwoo.android.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pollyannawu.justwoo.android.ui.home.HomeScreen
import com.pollyannawu.justwoo.android.ui.nav.house.HouseOnboardingContent
import com.pollyannawu.justwoo.android.ui.nav.tasks.TaskContent
import com.pollyannawu.justwoo.android.ui.nav.tasks.TaskQuickStatusOverlay
import com.pollyannawu.justwoo.android.ui.profile.ProfileEditScreen
import com.pollyannawu.justwoo.ui.nav.RootComponent

@Composable
fun RootContent(
    component: RootComponent,
    currentUserId: Long,
    currentHouseId: Long,
    isAdmin: Boolean = false,
) {
    CompositionLocalProvider(
        LocalAppActions provides AppActions(
            onProfileClick = component::onProfileClick,
            onCreateTaskClick = component::onCreateTaskClick,
            onTaskListClick = component::onTaskListClick,
            onTaskQuickClick = component::onTaskQuickClick,
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 底層：stack 頁面
            Children(
                stack = component.stack,
                animation = stackAnimation(fade() + slide()),
            ) { created ->
                when (val child = created.instance) {
                    is RootComponent.Child.Auth -> AuthContent(
                        component = child.component,
                    )
                    is RootComponent.Child.Home -> HomeRoute(
                        currentUserId = currentUserId,
                        currentHouseId = currentHouseId,
                        isAdmin = isAdmin,
                    )
                    is RootComponent.Child.Tasks -> TaskContent(
                        component = child.component,
                        currentUserId = currentUserId,
                        currentHouseId = currentHouseId,
                    )
                    is RootComponent.Child.Profile -> ProfileEditScreen(
                        onClose = child.component::onClose,
                    )
                    is RootComponent.Child.HouseOnboarding -> HouseOnboardingContent(
                        component = child.component,
                    )
                }
            }

            // 上層：ChildSlot 顯示 quick status overlay（slot 為空時不畫）
            val slot by component.taskQuickSlot.subscribeAsState()
            slot.child?.instance?.let { quickStatus ->
                TaskQuickStatusOverlay(component = quickStatus)
            }
        }
    }
}


@Composable
private fun HomeRoute(currentUserId: Long, currentHouseId: Long, isAdmin: Boolean) {
    val actions = LocalAppActions.current
    HomeScreen(
        currentUserId = currentUserId,
        currentHouseId = currentHouseId,
        onCreateTask = actions.onCreateTaskClick,
        onOpenTaskSpace = actions.onTaskListClick,
        onOpenCalendar = { /* TODO: calendar */ },
        onOpenProfile = actions.onProfileClick,
        onOpenMenu = { /* TODO: drawer */ },
        isAdmin = isAdmin,
        onInviteMember = { /* TODO: show GenerateInviteCodeSheet */ },
        onPendingRequests = { /* TODO: navigate to PendingRequestsScreen */ },
    )
}
