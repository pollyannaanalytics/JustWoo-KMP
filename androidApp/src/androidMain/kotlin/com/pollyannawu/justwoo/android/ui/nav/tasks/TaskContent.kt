package com.pollyannawu.justwoo.android.ui.nav.tasks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.pollyannawu.justwoo.android.ui.nav.LocalAppActions
import com.pollyannawu.justwoo.android.ui.task.CreateTaskScreen
import com.pollyannawu.justwoo.android.ui.task.TaskExplorationScreen
import com.pollyannawu.justwoo.ui.nav.tasks.TaskComponent
import com.pollyannawu.justwoo.ui.nav.tasks.TaskListComponent

@Composable
fun TaskContent(
    component: TaskComponent,
    currentUserId: Long,
    currentHouseId: Long,
) {
    Children(
        stack = component.stack,
        animation = stackAnimation(fade()),
    ) { created ->
        when (val child = created.instance) {
            is TaskComponent.Child.List -> TaskListRoute(
                currentUserId = currentUserId,
                currentHouseId = currentHouseId,
                component = child.component,
            )
            is TaskComponent.Child.Create -> CreateTaskScreen(
                currentUserId = currentUserId,
                currentHouseId = currentHouseId,
                component = child.component,
            )
            is TaskComponent.Child.Edit -> CreateTaskScreen(
                currentUserId = currentUserId,
                currentHouseId = currentHouseId,
                component = child.component,
            )
            is TaskComponent.Child.Details -> PlaceholderScreen(
                title = "Task #${child.component.taskId}",
            )
            is TaskComponent.Child.Assigned -> PlaceholderScreen(
                title = "Assigned to #${child.component.assigneeId}",
            )
        }
    }
}

@Composable
private fun TaskListRoute(
    currentUserId: Long,
    currentHouseId: Long,
    component: TaskListComponent,
) {
    val actions = LocalAppActions.current
    TaskExplorationScreen(
        currentUserId = currentUserId,
        currentHouseId = currentHouseId,
        component = component,
        onOpenProfile = actions.onProfileClick,
    )
}

@Composable
private fun PlaceholderScreen(title: String) {
    Text(
        text = "TODO: $title",
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(),
    )
}
