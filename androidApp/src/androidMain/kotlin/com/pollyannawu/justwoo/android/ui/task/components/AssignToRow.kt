package com.pollyannawu.justwoo.android.ui.task.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.task.CreateTaskViewModel
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.design.DesignTokens

@Composable
fun AssignToRow(
    assignees: List<CreateTaskViewModel.Assignee>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = if (selectedId == null) "Everyone"
    else assignees.firstOrNull { it.id == selectedId }?.label ?: "Member #$selectedId"

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(JustWooSpacing.Medium),
    ) {
        Text(
            text = "Assign to",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.Title.sp,
        )

        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(JustWooShapes.Small)
                    .border(1.dp, JustWooColors.Outline, JustWooShapes.Small)
                    .background(JustWooColors.CreamSurface)
                    .clickable { expanded = true }
                    .padding(horizontal = JustWooSpacing.Medium, vertical = JustWooSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedId == null) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = JustWooColors.UrgencyGreen,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(JustWooSpacing.Small))
                }
                Text(
                    text = selectedLabel,
                    color = JustWooColors.TextPrimary,
                    fontSize = DesignTokens.FontSize.BodyLarge.sp,
                    fontWeight = JustWooFontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Open",
                    tint = JustWooColors.TextSecondary,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(JustWooColors.CreamSurface),
            ) {
                DropdownMenuItem(
                    text = { Text("Everyone") },
                    onClick = { onSelect(null); expanded = false },
                )
                assignees.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a.label) },
                        onClick = { onSelect(a.id); expanded = false },
                    )
                }
            }
        }
    }
}

@Preview(name = "Everyone selected", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun AssignToRowEveryonePreview() {
    JustWooTheme {
        AssignToRow(
            assignees = PreviewSamples.assignees,
            selectedId = null,
            onSelect = {},
        )
    }
}

@Preview(name = "Member selected", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AssignToRowMemberPreview() {
    JustWooTheme {
        AssignToRow(
            assignees = PreviewSamples.assignees,
            selectedId = 1L,
            onSelect = {},
        )
    }
}
