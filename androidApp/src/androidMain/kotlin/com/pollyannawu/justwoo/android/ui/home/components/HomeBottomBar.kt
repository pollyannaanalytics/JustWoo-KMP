package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme

/**
 * Bottom navigation strip with a raised central FAB ("+") for creating a task.
 */
@Composable
fun HomeBottomBar(
    onOpenChat: () -> Unit,
    onOpenHome: () -> Unit,
    onCreateTask: () -> Unit,
    onOpenTaskSpace: () -> Unit,
    onOpenSettlements: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(JustWooColors.UrgencyYellowBg)
            .padding(horizontal = JustWooSpacing.Small, vertical = JustWooSpacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BottomBarIcon(Icons.Default.ChatBubbleOutline, "Chat", onOpenChat)
            BottomBarIcon(Icons.Default.Home, "Home", onOpenHome)
            Spacer(Modifier.size(JustWooSpacing.XXLarge + JustWooSpacing.XXLarge))
            BottomBarIcon(Icons.Outlined.Inbox, "Task Space", onOpenTaskSpace)
            BottomBarIcon(Icons.Default.AttachMoney, "Settlements", onOpenSettlements)
        }
        FloatingActionButton(
            onClick = onCreateTask,
            containerColor = JustWooColors.Primary,
            contentColor = JustWooColors.OnPrimary,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create task")
        }
    }
}

@Composable
private fun BottomBarIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = label, tint = JustWooColors.TextPrimary)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun HomeBottomBarPreview() {
    JustWooTheme {
        HomeBottomBar(
            onOpenChat = {},
            onOpenHome = {},
            onCreateTask = {},
            onOpenTaskSpace = {},
            onOpenSettlements = {},
        )
    }
}
