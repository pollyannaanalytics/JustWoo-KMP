package com.pollyannawu.justwoo.android.ui.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.domain.currency.CurrencyInfo
import com.pollyannawu.justwoo.domain.currency.availableCurrencies
import com.pollyannawu.justwoo.ui.nav.settlement.CurrencyPickerComponent

@Composable
fun CurrencyPickerScreen(component: CurrencyPickerComponent) {
    val currencies = remember { availableCurrencies() }
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isEmpty()) currencies
        else currencies.filter {
            it.code.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = JustWooSpacing.XSmall, vertical = JustWooSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = component::onClose) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = JustWooColors.TextPrimary)
            }
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = JustWooFontWeight.Bold,
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search by code or name") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = JustWooSpacing.XLarge, end = JustWooSpacing.XLarge, bottom = JustWooSpacing.Small),
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.code }) { info ->
                CurrencyRow(
                    info = info,
                    onClick = { component.onCurrencySelected(info.code) },
                )
                HorizontalDivider(color = JustWooColors.TextSecondary.copy(alpha = 0.12f))
            }
        }
    }
}

@Composable
private fun CurrencyRow(info: CurrencyInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JustWooSpacing.XLarge, vertical = JustWooSpacing.Default),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = info.code,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = JustWooFontWeight.SemiBold,
            )
            Text(
                text = info.name,
                style = MaterialTheme.typography.bodySmall,
                color = JustWooColors.TextSecondary,
            )
        }
        Text(
            text = info.symbol,
            style = MaterialTheme.typography.bodyMedium,
            color = JustWooColors.TextSecondary,
            modifier = Modifier.padding(start = JustWooSpacing.Default),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrencyPickerPreview() {
    MaterialTheme {
        CurrencyRow(
            info = CurrencyInfo(code = "TWD", symbol = "NT$", name = "New Taiwan Dollar"),
            onClick = {},
        )
    }
}
