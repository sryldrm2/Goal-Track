package com.esra.goaltrackproject.presentation.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.data.NotificationEntity

@Composable
fun NotificationSheetContent(
    notifications: List<NotificationEntity>,
    onMarkAsRead: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bildirimler", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Row {
                TextButton(onClick = onMarkAllRead) { Text("Hepsini Oku", fontSize = 12.sp, color = colorScheme.primary) }
                TextButton(onClick = onClearAll) { Text("Temizle", color = Color.Red, fontSize = 12.sp) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bildirim bulunmuyor.", color = colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkAsRead = { onMarkAsRead(notification.id) },
                        onDelete = { onDelete(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationEntity,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (notification.isRead) colorScheme.background else colorScheme.secondary.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colorScheme.onSurface)
                Text(notification.message, fontSize = 12.sp, color = colorScheme.onSurface.copy(alpha = 0.7f))
            }
            if (!notification.isRead) {
                IconButton(onClick = onMarkAsRead) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}