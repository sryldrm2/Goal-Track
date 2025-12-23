package com.esra.goaltrackproject.presentation.components.addgoal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TypeSelectionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = if (isSelected) colorScheme.primary else colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        border = if (isSelected) BorderStroke(2.dp, colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(desc, color = if (isSelected) colorScheme.onPrimary.copy(alpha = 0.7f) else colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = if (isSelected) colorScheme.onPrimary else colorScheme.primary))
        }
    }
}