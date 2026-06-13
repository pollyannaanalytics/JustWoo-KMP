package com.pollyannawu.justwoo.ui.nav.house

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface HouseOnboardingComponent : ComponentContext {
    val stack: Value<ChildStack<*, Child>>

    fun onJoinSelected()
    fun onCreateSelected()
    fun onBack()
    fun onCompleted()

    sealed interface Child {
        class SelectAction(val component: HouseOnboardingComponent) : Child
        class JoinHouse(val component: HouseOnboardingComponent) : Child
        class CreateHouse(val component: HouseOnboardingComponent) : Child
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
    override fun onBack() = navigation.pop()
    override fun onCompleted() = onCompleted.invoke()

    private fun createChild(
        config: Config,
        @Suppress("UNUSED_PARAMETER") childContext: ComponentContext,
    ): HouseOnboardingComponent.Child = when (config) {
        Config.SelectAction -> HouseOnboardingComponent.Child.SelectAction(this)
        Config.JoinHouse -> HouseOnboardingComponent.Child.JoinHouse(this)
        Config.CreateHouse -> HouseOnboardingComponent.Child.CreateHouse(this)
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object SelectAction : Config
        @Serializable data object JoinHouse : Config
        @Serializable data object CreateHouse : Config
    }
}
