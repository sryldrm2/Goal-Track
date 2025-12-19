package com.esra.goaltrackproject.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esra.goaltrackproject.data.SMARTGoalEntity
import com.esra.goaltrackproject.utils.toDateString
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onGoalClick: (Int) -> Unit,
    onAddGoalClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState(initial = DashboardUiState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Günaydın", style = MaterialTheme.typography.labelSmall)
                        Text("Merhaba, Esra", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Bildirimler */ }) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Bildirimler")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGoalClick,
                containerColor = Color(0xFFFDD835)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Hedef Ekle")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. İstatistik Bölümü
            item { SuccessAndStatsSection(state) }

            // 2. En Yakın Hedef
            state.nearestGoal?.let { goal ->
                item {
                    Text("Yaklaşan Hedef", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    NearestDeadlineGoalCard(goal, onGoalClick)
                }
            }

            // 3. Bugünün Hedefleri Başlığı
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bugünün Hedefleri",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4. Liste ve Boş Durum
            if (state.todayGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bugün için planlanmış bir hedefiniz bulunmuyor. Yeni bir hedef ekleyerek başlayın!",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(state.todayGoals) { goal ->
                    GoalItem(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SuccessAndStatsSection(state: DashboardUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Card(
            modifier = Modifier.weight(1.5f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDD835))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GENEL BAŞARI", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text("%${state.totalProgressPercent}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (state.totalProgressPercent > 50) "Harika gidiyorsun!" else "Hadi biraz hızlanalım!",
                    style = MaterialTheme.typography.bodySmall
                )
                LinearProgressIndicator(
                    progress = { state.totalProgressPercent / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = Color.Black,
                    trackColor = Color.Black.copy(alpha = 0.1f)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            StatCard(state.activeGoalsCount, "Aktif Hedef", Icons.Default.Circle, Color(0xFFFDD835))
            Spacer(Modifier.height(8.dp))
            StatCard(state.completedGoalsCount, "Tamamlanan", Icons.Default.CheckCircle, Color(0xFF8BC34A))
        }
    }
}

@Composable
fun StatCard(value: Int, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun NearestDeadlineGoalCard(goal: SMARTGoalEntity, onGoalClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoalClick(goal.id) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🚨 ACİL", color = Color.Red, style = MaterialTheme.typography.labelSmall)
            Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            val daysRemaining = TimeUnit.MILLISECONDS.toDays(goal.endDate - System.currentTimeMillis()).coerceAtLeast(0)
            Text("Bitiş: ${goal.endDate.toDateString()} ($daysRemaining gün kaldı)", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFFFDD835)
            )
        }
    }
}

@Composable
fun GoalItem(goal: SMARTGoalEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.Gray),
                modifier = Modifier.size(24.dp)
            ) {}
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = goal.title, fontWeight = FontWeight.Bold)
                Text(text = goal.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            Text(text = "🏃")
        }
    }
}