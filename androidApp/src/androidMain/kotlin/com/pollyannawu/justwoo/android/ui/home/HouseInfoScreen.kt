package com.pollyannawu.justwoo.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import kotlinx.datetime.Instant
import org.koin.androidx.compose.koinViewModel

@Composable
fun HouseInfoScreen(
    onClose: () -> Unit,
    viewModel: HouseInfoViewModel = koinViewModel(),
) {
    val house by viewModel.house.collectAsState(initial = null)
    val currentUserId by viewModel.currentUserId.collectAsState(initial = null)

    HouseInfoContent(
        house = house,
        currentUserId = currentUserId,
        onClose = onClose,
    )
}

@Composable
private fun HouseInfoContent(
    house: House?,
    currentUserId: Long?,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = JustWooSpacing.Small, vertical = JustWooSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = JustWooColors.TextPrimary)
            }
            Text(
                text = house?.name ?: "House Info",
                color = JustWooColors.TextPrimary,
                fontWeight = JustWooFontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = JustWooSpacing.Small),
            )
        }

        if (house == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = JustWooColors.TextSecondary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JustWooSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Large),
        ) {
            item {
                Spacer(Modifier.height(JustWooSpacing.Small))
                Text(
                    text = "About the House",
                    color = JustWooColors.TextPrimary,
                    fontWeight = JustWooFontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(JustWooSpacing.Small))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JustWooColors.CreamSurface)
                        .padding(JustWooSpacing.Medium),
                ) {
                    Text(
                        text = house.description.ifBlank { "No description." },
                        color = if (house.description.isBlank()) JustWooColors.TextPlaceholder else JustWooColors.TextPrimary,
                        fontSize = 14.sp,
                    )
                }
            }

            item {
                Text(
                    text = "Members (${house.members.size})",
                    color = JustWooColors.TextPrimary,
                    fontWeight = JustWooFontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(JustWooSpacing.Small))
            }

            items(house.members) { member ->
                MemberRow(member = member, isCurrentUser = member.userId == currentUserId)
            }

            item { Spacer(Modifier.height(JustWooSpacing.Large)) }
        }
    }
}

@Composable
private fun MemberRow(member: HouseMember, isCurrentUser: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JustWooColors.UrgencyYellowBg),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(JustWooSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Medium),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(JustWooColors.TextPrimary),
            )
            Text(
                text = "User #${member.userId}" + if (isCurrentUser) " (you)" else "",
                color = JustWooColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = if (isCurrentUser) JustWooFontWeight.Bold else JustWooFontWeight.Regular,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 800)
@Composable
private fun HouseInfoPreview() {
    JustWooTheme {
        HouseInfoContent(
            house = House(
                id = 1L,
                name = "Sunny Apartment",
                avatar = "",
                description = "A cozy place for four friends.",
                members = listOf(
                    HouseMember(houseId = 1L, userId = 101L, role = MemberRole.ADMIN, joinedAt = Instant.fromEpochSeconds(0)),
                    HouseMember(houseId = 1L, userId = 102L, role = MemberRole.MEMBER, joinedAt = Instant.fromEpochSeconds(0)),
                ),
                createTime = Instant.fromEpochSeconds(0),
                updateTime = Instant.fromEpochSeconds(0),
            ),
            currentUserId = 101L,
            onClose = {},
        )
    }
}
