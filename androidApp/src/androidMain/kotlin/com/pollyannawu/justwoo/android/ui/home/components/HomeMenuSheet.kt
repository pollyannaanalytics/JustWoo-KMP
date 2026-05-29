package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuSheet(
    onDismiss: () -> Unit,
    onHouseInfo: () -> Unit,
    onProfileEdit: () -> Unit,
    onLeaveHouse: () -> Unit,
    onLogOut: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = JustWooColors.Cream,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = JustWooSpacing.Large),
        ) {
            MenuRow(
                icon = Icons.Default.Home,
                label = "House Info",
                onClick = onHouseInfo,
            )
            HorizontalDivider(color = JustWooColors.TextPrimary.copy(alpha = 0.08f))
            MenuRow(
                icon = Icons.Default.Person,
                label = "Profile Edit",
                onClick = onProfileEdit,
            )
            HorizontalDivider(color = JustWooColors.TextPrimary.copy(alpha = 0.08f))
            MenuRow(
                icon = Icons.Outlined.RemoveCircleOutline,
                label = "Leave House",
                onClick = onLeaveHouse,
                tint = JustWooColors.Error,
            )
            HorizontalDivider(color = JustWooColors.TextPrimary.copy(alpha = 0.08f))
            MenuRow(
                icon = Icons.Default.ExitToApp,
                label = "Log Out",
                onClick = onLogOut,
                tint = JustWooColors.Error,
            )
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = JustWooColors.TextPrimary,
) {
    ListItem(
        headlineContent = {
            Text(text = label, color = tint)
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = tint)
        },
        colors = ListItemDefaults.colors(containerColor = JustWooColors.Cream),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun HomeMenuSheetPreview() {
    JustWooTheme {
        Column {
            MenuRow(Icons.Default.Home, "House Info", {})
            HorizontalDivider()
            MenuRow(Icons.Default.Person, "Profile Edit", {})
            HorizontalDivider()
            MenuRow(Icons.Outlined.RemoveCircleOutline, "Leave House", {}, JustWooColors.Error)
            HorizontalDivider()
            MenuRow(Icons.Default.ExitToApp, "Log Out", {}, JustWooColors.Error)
        }
    }
}
