package com.pollyannawu.justwoo.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileEditScreen(
    onClose: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = JustWooSpacing.XSmall, vertical = JustWooSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
            }
            Text(
                text = "Edit profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = JustWooFontWeight.Bold,
            )
        }

        Column(modifier = Modifier.padding(horizontal = JustWooSpacing.XLarge).fillMaxSize()) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(JustWooColors.UrgencyYellowBg)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    state.name.take(1).uppercase().ifBlank { "W" },
                    fontWeight = JustWooFontWeight.Black,
                    style = MaterialTheme.typography.displayMedium,
                    color = JustWooColors.PrimaryDeep,
                )
            }
            Spacer(Modifier.height(JustWooSpacing.XLarge))

            Text("Name", fontWeight = JustWooFontWeight.SemiBold)
            Spacer(Modifier.height(JustWooSpacing.Small))
            JustWooTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "Your name",
                isError = state.nameError != null,
                errorMessage = state.nameError,
                modifier = Modifier.fillMaxWidth()
            )
            CounterHint(state.name.length, ProfileEditViewModel.NAME_LIMIT)
            Spacer(Modifier.height(JustWooSpacing.Default))

            Text("Bio", fontWeight = JustWooFontWeight.SemiBold)
            Spacer(Modifier.height(JustWooSpacing.Small))
            JustWooTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChange,
                placeholder = "A few words about you",
                isError = state.bioError != null,
                errorMessage = state.bioError,
                singleLine = false,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            CounterHint(state.bio.length, ProfileEditViewModel.BIO_LIMIT)
            Spacer(Modifier.height(JustWooSpacing.Default))

            Text("Hashtags", fontWeight = JustWooFontWeight.SemiBold)
            Spacer(Modifier.height(JustWooSpacing.Small))
            Row(verticalAlignment = Alignment.CenterVertically) {
                JustWooTextField(
                    value = state.newHashtag,
                    onValueChange = viewModel::onNewHashtagChange,
                    placeholder = "add #hashtag",
                    isError = state.hashtagError != null,
                    errorMessage = state.hashtagError,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(JustWooSpacing.Small))
                Box(
                    modifier = Modifier
                        .clip(JustWooShapes.XLarge)
                        .background(JustWooColors.Primary)
                        .clickable { viewModel.addHashtag() }
                        .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Medium)
                ) {
                    Text("Add", color = JustWooColors.OnPrimary, fontWeight = JustWooFontWeight.Bold)
                }
            }
            Spacer(Modifier.height(JustWooSpacing.Small))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Small)) {
                items(state.hashtags, key = { it }) { tag ->
                    HashtagChip(tag = tag, onRemove = { viewModel.removeHashtag(tag) })
                }
            }

            Spacer(Modifier.height(JustWooSpacing.XLarge))
            JustWooPrimaryButton(
                text = "Save",
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CounterHint(current: Int, max: Int) {
    Text(
        text = "$current/$max",
        color = if (current > max) JustWooColors.Error else JustWooColors.TextSecondary,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = JustWooSpacing.Medium, top = 2.dp),
    )
}

@Composable
private fun HashtagChip(tag: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(JustWooShapes.Large)
            .background(JustWooColors.UrgencyYellowBg)
            .padding(horizontal = JustWooSpacing.Medium, vertical = JustWooSpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#$tag", color = JustWooColors.PrimaryDeep, fontWeight = JustWooFontWeight.SemiBold)
        Spacer(Modifier.width(JustWooSpacing.XSmall))
        Icon(
            Icons.Default.Close,
            contentDescription = "Remove",
            tint = JustWooColors.PrimaryDeep,
            modifier = Modifier.size(16.dp).clickable(onClick = onRemove)
        )
    }
}
