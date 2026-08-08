package com.pollyannawu.justwoo.ui.nav.house

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface OtpSessionComponent {
    fun onBack()
}

class DefaultOtpSessionComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : OtpSessionComponent, ComponentContext by componentContext {
    override fun onBack() = onBack.invoke()
}

interface ConfirmOtpInviteComponent {
    fun onBack()
}

class DefaultConfirmOtpInviteComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : ConfirmOtpInviteComponent, ComponentContext by componentContext {
    override fun onBack() = onBack.invoke()
}

interface HouseOnboardingComponent : ComponentContext {
    val stack: Value<ChildStack<*, Child>>

    fun onJoinSelected()
    fun onCreateSelected()
    fun onMyInvitationsSelected()
    fun onShowMyCodeSelected()
    fun onConfirmOtpInviteSelected()
    fun onBack()
    fun onCompleted()

    sealed interface Child {
        class SelectAction(val component: HouseOnboardingComponent) : Child
        class JoinHouse(val component: HouseOnboardingComponent) : Child
        class CreateHouse(val component: HouseOnboardingComponent) : Child
        class PendingInvitations(val component: HouseOnboardingComponent) : Child
        class OtpSession(val component: OtpSessionComponent) : Child
        class ConfirmOtpInvite(val component: ConfirmOtpInviteComponent) : Child
    }
}

class DefaultHouseOnboardingComponent(
    componentContext: ComponentContext,
    private val onCompleted: () -> Unit,
) : HouseOnboardingComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, HouseOnboardingComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.SelectAction,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override fun onJoinSelected() = navigation.push(Config.JoinHouse)
    override fun onCreateSelected() = navigation.push(Config.CreateHouse)
    override fun onMyInvitationsSelected() = navigation.push(Config.PendingInvitations)
    override fun onShowMyCodeSelected() = navigation.push(Config.OtpSession)
    override fun onConfirmOtpInviteSelected() = navigation.push(Config.ConfirmOtpInvite)
    override fun onBack() = navigation.pop()
    override fun onCompleted() = onCompleted.invoke()

    private fun createChild(
        config: Config,
        childContext: ComponentContext,
    ): HouseOnboardingComponent.Child = when (config) {
        Config.SelectAction -> HouseOnboardingComponent.Child.SelectAction(this)
        Config.JoinHouse -> HouseOnboardingComponent.Child.JoinHouse(this)
        Config.CreateHouse -> HouseOnboardingComponent.Child.CreateHouse(this)
        Config.PendingInvitations -> HouseOnboardingComponent.Child.PendingInvitations(this)
        Config.OtpSession -> HouseOnboardingComponent.Child.OtpSession(
            DefaultOtpSessionComponent(childContext, onBack = ::onBack)
        )
        Config.ConfirmOtpInvite -> HouseOnboardingComponent.Child.ConfirmOtpInvite(
            DefaultConfirmOtpInviteComponent(childContext, onBack = ::onBack)
        )
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object SelectAction : Config
        @Serializable data object JoinHouse : Config
        @Serializable data object CreateHouse : Config
        @Serializable data object PendingInvitations : Config
        @Serializable data object OtpSession : Config
        @Serializable data object ConfirmOtpInvite : Config
    }
}
