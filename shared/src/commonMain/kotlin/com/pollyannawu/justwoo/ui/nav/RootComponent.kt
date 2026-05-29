package com.pollyannawu.justwoo.ui.nav

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.pollyannawu.justwoo.ui.nav.auth.AuthComponent
import com.pollyannawu.justwoo.ui.nav.auth.AuthStart
import com.pollyannawu.justwoo.ui.nav.auth.DefaultAuthComponent
import com.pollyannawu.justwoo.ui.nav.home.DefaultHomeComponent
import com.pollyannawu.justwoo.ui.nav.home.HomeComponent
import com.pollyannawu.justwoo.ui.nav.house.DefaultHouseOnboardingComponent
import com.pollyannawu.justwoo.ui.nav.house.HouseOnboardingComponent
import com.pollyannawu.justwoo.ui.nav.houseinfo.DefaultHouseInfoComponent
import com.pollyannawu.justwoo.ui.nav.houseinfo.HouseInfoComponent
import com.pollyannawu.justwoo.ui.nav.profile.DefaultProfileComponent
import com.pollyannawu.justwoo.ui.nav.profile.ProfileComponent
import com.pollyannawu.justwoo.ui.nav.tasks.DefaultTaskComponent
import com.pollyannawu.justwoo.ui.nav.tasks.DefaultTaskQuickStatusComponent
import com.pollyannawu.justwoo.ui.nav.tasks.TaskComponent
import com.pollyannawu.justwoo.ui.nav.tasks.TaskQuickStatusComponent
import kotlinx.serialization.Serializable

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    val taskQuickSlot: Value<ChildSlot<*, TaskQuickStatusComponent>>

    fun onHomeClick()
    fun onProfileClick()
    fun onTaskListClick()
    fun onCreateTaskClick()
    fun onTaskQuickClick(taskId: Long)
    fun onHouseInfoClick()
    fun onSessionChanged(isAuthenticated: Boolean)
    fun onCheckingHouse()
    fun onHouseOnboardingRequired()
    fun onHouseOnboardingComplete()

    sealed interface Child {
        data object Loading : Child
        class Auth(val component: AuthComponent) : Child
        class Home(val component: HomeComponent) : Child
        class Tasks(val component: TaskComponent) : Child
        class Profile(val component: ProfileComponent) : Child
        class HouseOnboarding(val component: HouseOnboardingComponent) : Child
        class HouseInfo(val component: HouseInfoComponent) : Child
    }
}

/**
 * Root container that owns the visible stack. It is intentionally
 * unaware of `AuthRepository` — the host activity/ViewModel reads
 * session state and feeds it in via the constructor's
 * [initiallyAuthenticated] flag and the [onSessionChanged] hook.
 */
class DefaultRootComponent(
    componentContext: ComponentContext,
    initiallyAuthenticated: Boolean,
    startInLoading: Boolean = false,
    private val authStartProvider: () -> AuthStart = { AuthStart.SignIn },
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()
    private val slotNavigation = SlotNavigation<SlotConfig>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = when {
                startInLoading -> Config.Loading
                initiallyAuthenticated -> Config.Home
                else -> Config.Auth
            },
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override val taskQuickSlot: Value<ChildSlot<*, TaskQuickStatusComponent>> =
        childSlot(
            source = slotNavigation,
            serializer = SlotConfig.serializer(),
            handleBackButton = true,
            childFactory = ::createSlotChild,
        )

    override fun onSessionChanged(isAuthenticated: Boolean) {
        val current = stack.value.active.configuration
        when {
            isAuthenticated && current is Config.Auth ->
                navigation.replaceAll(Config.Home)

            !isAuthenticated && current !is Config.Auth ->
                navigation.replaceAll(Config.Auth)
        }
    }

    override fun onCheckingHouse() {
        if (stack.value.active.configuration !is Config.Loading) {
            navigation.replaceAll(Config.Loading)
        }
    }

    override fun onHouseOnboardingRequired() {
        if (stack.value.active.configuration !is Config.HouseOnboarding) {
            navigation.replaceAll(Config.HouseOnboarding)
        }
    }

    override fun onHouseOnboardingComplete() {
        if (stack.value.active.configuration !is Config.Home) {
            navigation.replaceAll(Config.Home)
        }
    }

    override fun onHomeClick() {
        navigation.bringToFront(Config.Home)
    }

    override fun onProfileClick() {
        navigation.bringToFront(Config.Profile)
    }

    override fun onTaskListClick() {
        navigation.bringToFront(Config.Tasks())
    }

    override fun onCreateTaskClick() {
        navigation.push(Config.Tasks(startOnCreate = true))
    }

    override fun onTaskQuickClick(taskId: Long) {
        slotNavigation.activate(SlotConfig.QuickStatus(taskId))
    }

    override fun onHouseInfoClick() {
        navigation.bringToFront(Config.HouseInfo)
    }

    private fun createChild(
        config: Config,
        childContext: ComponentContext,
    ): RootComponent.Child = when (config) {
        Config.Loading -> RootComponent.Child.Loading
        Config.Auth -> RootComponent.Child.Auth(
            DefaultAuthComponent(
                componentContext = childContext,
                initialScreen = authStartProvider(),
            ),
        )

        Config.Home -> RootComponent.Child.Home(
            DefaultHomeComponent(componentContext = childContext),
        )

        is Config.Tasks -> RootComponent.Child.Tasks(
            DefaultTaskComponent(
                componentContext = childContext,
                onExit = { navigation.pop() },
                startOnCreate = config.startOnCreate,
            ),
        )

        Config.Profile -> RootComponent.Child.Profile(
            DefaultProfileComponent(
                componentContext = childContext,
                onFinished = { navigation.pop() },
            ),
        )

        Config.HouseOnboarding -> RootComponent.Child.HouseOnboarding(
            DefaultHouseOnboardingComponent(
                componentContext = childContext,
                onCompleted = { onHouseOnboardingComplete() },
            )
        )

        Config.HouseInfo -> RootComponent.Child.HouseInfo(
            DefaultHouseInfoComponent(
                componentContext = childContext,
                onClose = { navigation.pop() },
            ),
        )
    }

    private fun createSlotChild(
        config: SlotConfig,
        childContext: ComponentContext,
    ): TaskQuickStatusComponent = when (config) {
        is SlotConfig.QuickStatus -> DefaultTaskQuickStatusComponent(
            componentContext = childContext,
            taskId = config.taskId,
            onDismiss = { slotNavigation.dismiss() },
        )
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Loading : Config
        @Serializable
        data object Auth : Config
        @Serializable
        data object Home : Config
        @Serializable
        data class Tasks(val startOnCreate: Boolean = false) : Config
        @Serializable
        data object Profile : Config
        @Serializable
        data object HouseOnboarding : Config
        @Serializable
        data object HouseInfo : Config

    }

    @Serializable
    private sealed interface SlotConfig {
        @Serializable
        data class QuickStatus(val taskId: Long) : SlotConfig
    }
}
