package com.esra.goaltrackproject.presentation.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(title: String, tag: String?) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)) {
        Text(title, color = colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        if (tag != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(color = colorScheme.onSurface.copy(alpha = 0.05f), shape = RoundedCornerShape(4.dp)) {
                Text(tag, color = colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}
