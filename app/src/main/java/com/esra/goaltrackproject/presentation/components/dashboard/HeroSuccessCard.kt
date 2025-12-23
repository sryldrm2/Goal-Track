package com.esra.goaltrackproject.presentation.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HeroSuccessCard(totalProgress: Int) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Günlük Başarı", color = colorScheme.onPrimary.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                Text(
                    text = "%$totalProgress",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onPrimary
                )
                Text("Tamamlandı", color = colorScheme.onPrimary.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { totalProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = colorScheme.secondary,
                    trackColor = colorScheme.onPrimary.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
