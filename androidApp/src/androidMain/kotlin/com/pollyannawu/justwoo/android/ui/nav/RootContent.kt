package com.pollyannawu.justwoo.android.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pollyannawu.justwoo.android.ui.home.HomeScreen
import com.pollyannawu.justwoo.android.ui.home.HouseInfoScreen
import com.pollyannawu.justwoo.android.ui.nav.house.HouseOnboardingContent
import com.pollyannawu.justwoo.android.ui.nav.tasks.TaskContent
import com.pollyannawu.justwoo.android.ui.nav.tasks.TaskQuickStatusOverlay
import com.pollyannawu.justwoo.android.ui.profile.ProfileEditScreen
import com.pollyannawu.justwoo.domain.usecase.auth.LogoutUseCase
import com.pollyannawu.justwoo.domain.usecase.house.LeaveHouseUseCase
import com.pollyannawu.justwoo.ui.nav.RootComponent
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun RootContent(
    component: RootComponent,
    currentUserId: Long,
    currentHouseId: Long,
) {
    val scope = rememberCoroutineScope()
    val logoutUseCase: LogoutUseCase = koinInject()
    val leaveHouseUseCase: LeaveHouseUseCase = koinInject()

    CompositionLocalProvider(
        LocalAppActions provides AppActions(
            onHomeClick = component::onHomeClick,
            onProfileClick = component::onProfileClick,
            onCreateTaskClick = component::onCreateTaskClick,
            onTaskListClick = component::onTaskListClick,
            onTaskQuickClick = component::onTaskQuickClick,
            onHouseInfoClick = component::onHouseInfoClick,
            onLogOut = {
                scope.launch {
                    logoutUseCase()
                    component.onSessionChanged(false)
                }
            },
            onLeaveHouse = {
                scope.launch {
                    runCatching { leaveHouseUseCase(currentHouseId, currentUserId) }
                    component.onSessionChanged(false)
                }
            },
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Children(
                stack = component.stack,
                animation = stackAnimation(fade() + slide()),
            ) { created ->
                when (val child = created.instance) {
                    RootComponent.Child.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }

                    is RootComponent.Child.Auth -> AuthContent(
                        component = child.component,
                    )

                    is RootComponent.Child.Home -> MainShell { padding ->
                        HomeScreen(
                            currentUserId = currentUserId,
                            currentHouseId = currentHouseId,
                            padding = padding,
                            onOpenTaskSpace = component::onTaskListClick,
                        )
                    }

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

                    is RootComponent.Child.HouseInfo -> HouseInfoScreen(
                        onClose = child.component::onClose,
                    )
                }
            }

            val slot by component.taskQuickSlot.subscribeAsState()
            slot.child?.instance?.let { quickStatus ->
                TaskQuickStatusOverlay(component = quickStatus)
            }
        }
    }
}
