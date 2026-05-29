package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.common.JustWooLogo
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme

@Composable
fun HomeTopBar(
    onOpenMenu: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onInviteMember: () -> Unit = {},
    onPendingRequests: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JustWooColors.Cream)
            .padding(horizontal = JustWooSpacing.Medium, vertical = JustWooSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenMenu) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = JustWooColors.TextPrimary)
        }
        Spacer(Modifier.weight(1f))
        JustWooLogo(modifier = Modifier.height(32.dp))
        Spacer(Modifier.weight(1f))
        if (isAdmin) {
            IconButton(onClick = onInviteMember) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Invite member", tint = JustWooColors.TextPrimary)
            }
            IconButton(onClick = onPendingRequests) {
                Icon(Icons.Default.People, contentDescription = "Pending requests", tint = JustWooColors.TextPrimary)
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(JustWooColors.TextPrimary)
                .clickable { onOpenProfile() },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun HomeTopBarPreview() {
    JustWooTheme {
        HomeTopBar(onOpenMenu = {}, onOpenProfile = {})
    }
}
