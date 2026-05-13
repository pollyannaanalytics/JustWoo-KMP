package com.pollyannawu.justwoo.android.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors

/**
 * Rounded-pill input matching the Sign in screen in Figma:
 * 1px stroke, cream fill, placeholder text, red stroke + red message on error.
 */
@Composable
fun JustWooTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = JustWooColors.TextPlaceholder) },
            isError = isError,
            singleLine = singleLine,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = JustWooColors.CreamSurface,
                unfocusedContainerColor = JustWooColors.CreamSurface,
                errorContainerColor = JustWooColors.CreamSurface,
                focusedBorderColor = JustWooColors.OutlineFocused,
                unfocusedBorderColor = JustWooColors.Outline,
                errorBorderColor = JustWooColors.Error,
                cursorColor = JustWooColors.TextPrimary,
                focusedTextColor = JustWooColors.TextPrimary,
                unfocusedTextColor = JustWooColors.TextPrimary,
            )
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = "*$errorMessage",
                color = JustWooColors.Error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun JustWooPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    showPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    JustWooTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        isError = isError,
        errorMessage = errorMessage,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
    )
}
