package com.pollyannawu.justwoo.nav

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.pollyannawu.justwoo.session.SessionState
import kotlinx.serialization.Serializable

/**
 * Decompose root. Lives in shared/commonMain so both Android and iOS can host
 * the same stack. Children are described by [Child]; concrete navigation
 * actions are exposed by the per-feature components in [NavComponents].
 */
interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        class SignIn(val component: SignInComponent) : Child()
        class Register(val component: RegisterComponent) : Child()
        class Home(val component: HomeComponent) : Child()
        class CreateTask(val component: CreateTaskComponent) : Child()
        class TaskExploration(val component: TaskExplorationComponent) : Child()
        class Calendar(val component: CalendarComponent) : Child()
        class ProfileEdit(val component: ProfileEditComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val session: SessionState,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.SignIn,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(
        config: Config,
        componentContext: ComponentContext,
    ): RootComponent.Child = when (config) {
        Config.SignIn -> RootComponent.Child.SignIn(signInComponent())
        Config.Register -> RootComponent.Child.Register(registerComponent())
        Config.Home -> RootComponent.Child.Home(homeComponent())
        Config.CreateTask -> RootComponent.Child.CreateTask(createTaskComponent())
        Config.TaskExploration -> RootComponent.Child.TaskExploration(taskExplorationComponent())
        Config.Calendar -> RootComponent.Child.Calendar(calendarComponent())
        Config.ProfileEdit -> RootComponent.Child.ProfileEdit(profileEditComponent())
    }

    private fun signInComponent() = object : SignInComponent {
        override fun onSignInSuccess() {
            navigation.replaceAll(Config.Home)
        }
        override fun onNavigateToRegister() {
            navigation.push(Config.Register)
        }
    }

    private fun registerComponent() = object : RegisterComponent {
        override fun onRegisterSuccess() {
            navigation.replaceAll(Config.Home)
        }
        override fun onNavigateToSignIn() {
            navigation.pop()
        }
    }

    private fun homeComponent() = object : HomeComponent {
        override val userId: Long get() = session.user.value?.id ?: 0L
        override val houseId: Long get() = session.houseId.value ?: 0L
        override fun onCreateTask() { navigation.push(Config.CreateTask) }
        override fun onOpenTaskSpace() { navigation.push(Config.TaskExploration) }
        override fun onOpenCalendar() { navigation.push(Config.Calendar) }
        override fun onOpenProfile() { navigation.push(Config.ProfileEdit) }
        override fun onOpenMenu() { /* drawer not in scope */ }
    }

    private fun createTaskComponent() = object : CreateTaskComponent {
        override val userId: Long get() = session.user.value?.id ?: 0L
        override val houseId: Long get() = session.houseId.value ?: 0L
        override fun onClose() { navigation.pop() }
        override fun onOpenProfile() { navigation.bringToFront(Config.ProfileEdit) }
    }

    private fun taskExplorationComponent() = object : TaskExplorationComponent {
        override val userId: Long get() = session.user.value?.id ?: 0L
        override val houseId: Long get() = session.houseId.value ?: 0L
        override fun onClose() { navigation.pop() }
        override fun onOpenProfile() { navigation.bringToFront(Config.ProfileEdit) }
    }

    private fun calendarComponent() = object : CalendarComponent {
        override fun onClose() { navigation.pop() }
        override fun onOpenTask(taskId: Long) { /* TODO: task detail route */ }
    }

    private fun profileEditComponent() = object : ProfileEditComponent {
        override fun onClose() { navigation.pop() }
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object SignIn : Config
        @Serializable data object Register : Config
        @Serializable data object Home : Config
        @Serializable data object CreateTask : Config
        @Serializable data object TaskExploration : Config
        @Serializable data object Calendar : Config
        @Serializable data object ProfileEdit : Config
    }
}
