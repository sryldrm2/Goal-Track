package com.esra.goaltrackproject.presentation.components.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = colorScheme.onSurface.copy(alpha = 0.5f)) },
        label = label?.let { { Text(it, color = colorScheme.onSurface.copy(alpha = 0.7f)) } },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface,
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = colorScheme.onSurface.copy(alpha = 0.7f),
            unfocusedLabelColor = colorScheme.onSurface.copy(alpha = 0.5f),
            cursorColor = colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}