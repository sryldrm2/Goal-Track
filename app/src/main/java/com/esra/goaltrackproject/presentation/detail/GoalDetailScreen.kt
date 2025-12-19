package com.esra.goaltrackproject.presentation.detail

import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.data.SMARTGoalEntity
import kotlin.math.roundToInt
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun GoalDetailScreen(
    goalId: Int,
    viewModel: GoalDetailViewModel,
    onDeleteSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadGoal(goalId)
    }

    val goal by viewModel.goal.collectAsState()
    val context = LocalContext.current

    if (goal == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            GoalDetailAppBar(
                goal = goal!!,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onDeleteSuccess = onDeleteSuccess,
                context = context
            )
        }
    ) { padding ->
        GoalDetailContent(
            goal = goal!!,
            viewModel = viewModel,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailAppBar(
    goal: SMARTGoalEntity,
    viewModel: GoalDetailViewModel,
    onNavigateBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    context: android.content.Context
) {

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hedefi Sil") },
            text = { Text("${goal.title} hedefini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal.id)
                    // Raporunda belirttiğin bildirim iptali işini ViewModel içinde yapman en iyisidir
                    Toast.makeText(context, "${goal.title} Silindi", Toast.LENGTH_SHORT).show()
                    onDeleteSuccess()
                    showDeleteDialog = false
                }) { Text("Sil", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Vazgeç") }
            }
        )
    }
    TopAppBar(
        title = { Text(text = "Hedef Detayı") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
        },
        actions = {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hedefi Sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun GoalDetailContent(
    goal: SMARTGoalEntity,
    viewModel: GoalDetailViewModel,
    modifier: Modifier = Modifier
) {
    val progress = goal.progressPercent.toFloat() / 100f
    val progressPercent = goal.progressPercent

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = goal.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = goal.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                strokeWidth = 10.dp,
                modifier = Modifier.size(150.dp),
                color = Color(0xFFEAB308),
                trackColor = Color.LightGray.copy(alpha = 0.2f)
            )
            Text(
                text = "%$progressPercent",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Harika gidiyorsun!",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        DayStatisticsSection(goal)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Bu Hafta", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        WeeklyProgressPlaceHolder()

        Spacer(modifier = Modifier.height(24.dp))

        Text("Son Notum", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        GoalNoteCard(note = goal.note)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.markTodayCompleted()
            },
            enabled = goal.currentValue < goal.targetValue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Bugünü Tamamladım")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DayStatisticsSection(goal: SMARTGoalEntity) {
    val totalDays = ((goal.endDate - goal.startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
    val completedDays = ((System.currentTimeMillis() - goal.startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    val remainingDays = (totalDays - completedDays).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard("Tamamlanan", completedDays, "Gün", Color(0xFFEAB308))
        StatCard("Toplam", totalDays, "Gün", Color(0xFF475569))
        StatCard("Kalan", remainingDays, "Gün", Color(0xFFEF4444))
    }
}

@Composable
fun WeeklyProgressPlaceHolder() {
    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier.size(35.dp),
                    color = Color(0xFFEAB308),
                    strokeWidth = 3.dp,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = day, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun GoalNoteCard(note: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFACC15).copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Hedef Notu",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (note.isNullOrBlank()) "Henüz bir not eklenmemiş." else note,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: Int,
    unit: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier.size(90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(text = unit, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
















