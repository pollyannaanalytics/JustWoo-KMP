package com.pollyannawu.justwoo.ui.nav.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

/**
 * Two-screen Decompose sub-stack for the unauthenticated flow.
 */
interface AuthComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {
        class SignIn(val component: SignInComponent) : Child
        class Register(val component: RegisterComponent) : Child
    }
}

interface SignInComponent {
    fun onSwitchToRegister()
}

interface RegisterComponent {
    fun onSwitchToSignIn()
}

/**
 * Picks which screen the auth sub-stack opens on. Returning users land
 * on Sign in; fresh installs go to Register (onboarding-first flow).
 */
enum class AuthStart { SignIn, Register }

class DefaultAuthComponent(
    componentContext: ComponentContext,
    initialScreen: AuthStart = AuthStart.SignIn,
) : AuthComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, AuthComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = initialScreen.toConfig(),
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(
        config: Config,
        childContext: ComponentContext,
    ): AuthComponent.Child = when (config) {
        Config.SignIn -> AuthComponent.Child.SignIn(
            DefaultSignInComponent(
                onSwitchToRegister = { navigation.replaceCurrent(Config.Register) },
            ),
        )
        Config.Register -> AuthComponent.Child.Register(
            DefaultRegisterComponent(
                onSwitchToSignIn = { navigation.replaceCurrent(Config.SignIn) },
            ),
        )
    }

    private class DefaultSignInComponent(
        private val onSwitchToRegister: () -> Unit,
    ) : SignInComponent {
        override fun onSwitchToRegister() = onSwitchToRegister.invoke()
    }

    private class DefaultRegisterComponent(
        private val onSwitchToSignIn: () -> Unit,
    ) : RegisterComponent {
        override fun onSwitchToSignIn() = onSwitchToSignIn.invoke()
    }

    private fun AuthStart.toConfig(): Config = when (this) {
        AuthStart.SignIn -> Config.SignIn
        AuthStart.Register -> Config.Register
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object SignIn : Config
        @Serializable data object Register : Config
    }
}
