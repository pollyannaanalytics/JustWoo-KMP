package com.pollyannawu.justwoo.ui.nav.tasks

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface TaskComponent {
    val stack: Value<ChildStack<*, Child>>

    fun onCreateTaskClick()
    fun onEditTaskClick(taskId: Long)
    fun onTaskStatusClick(taskId: Long)
    fun onAssignedTaskClick(assigneeId: Long)

    sealed interface Child {
        class List(val component: TaskListComponent) : Child
        class Create(val component: TaskEditComponent) : Child
        class Edit(val component: TaskEditComponent) : Child
        class Details(val component: TaskDetailsComponent) : Child
        class Assigned(val component: AssignedTasksComponent) : Child
    }
}

class DefaultTaskComponent(
    componentContext: ComponentContext,
    private val onExit: () -> Unit,
) : TaskComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, TaskComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.List,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override fun onCreateTaskClick() {
        navigation.push(Config.Create)
    }

    override fun onEditTaskClick(taskId: Long) {
        navigation.push(Config.Edit(taskId))
    }

    override fun onTaskStatusClick(taskId: Long) {
        navigation.push(Config.Details(taskId))
    }

    override fun onAssignedTaskClick(assigneeId: Long) {
        navigation.push(Config.Assigned(assigneeId))
    }

    private fun createChild(
        config: Config,
        childContext: ComponentContext,
    ): TaskComponent.Child = when (config) {
        Config.List -> TaskComponent.Child.List(
            DefaultTaskListComponent(
                componentContext = childContext,
                onFinished = { popOrExit() },
            ),
        )
        Config.Create -> TaskComponent.Child.Create(
            DefaultTaskEditComponent(
                componentContext = childContext,
                editingTaskId = null,
                onFinished = { popOrExit() },
            ),
        )
        is Config.Edit -> TaskComponent.Child.Edit(
            DefaultTaskEditComponent(
                componentContext = childContext,
                editingTaskId = config.taskId,
                onFinished = { popOrExit() },
            ),
        )
        is Config.Details -> TaskComponent.Child.Details(
            DefaultTaskDetailsComponent(
                componentContext = childContext,
                taskId = config.taskId,
                onFinished = { popOrExit() },
            ),
        )
        is Config.Assigned -> TaskComponent.Child.Assigned(
            DefaultAssignedTasksComponent(
                componentContext = childContext,
                assigneeId = config.assigneeId,
                onFinished = { popOrExit() },
            ),
        )
    }

    private fun popOrExit() {
        navigation.pop { popped ->
            if (!popped) onExit()
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object List : Config
        @Serializable data object Create : Config
        @Serializable data class Edit(val taskId: Long) : Config
        @Serializable data class Details(val taskId: Long) : Config
        @Serializable data class Assigned(val assigneeId: Long) : Config
    }
}
