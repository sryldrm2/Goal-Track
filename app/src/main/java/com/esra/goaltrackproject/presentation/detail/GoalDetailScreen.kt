package com.esra.goaltrackproject.presentation.detail

import android.app.TimePickerDialog
import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import com.esra.goaltrackproject.data.DailyProgressEntity
import java.util.Calendar

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
    val weeklyProgress by viewModel.weeklyProgress.collectAsState()
    val dailyValue by viewModel.dailyValue.collectAsState()
    val completedDaysCount by viewModel.completedDaysCount.collectAsState()
    val context = LocalContext.current

    val backgroundLight = Color(0xFFF8F8F5)

    if (goal == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFDD835))
        }
        return
    }

    Scaffold(
        containerColor = backgroundLight,
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
            weeklyProgress = weeklyProgress,
            dailyValue = dailyValue,
            completedDaysCount = completedDaysCount,
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
    var showEditSheet by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hedefi Sil") },
            text = { Text("${goal.title} hedefini silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal.id)
                    Toast.makeText(context, "Silindi", Toast.LENGTH_SHORT).show()
                    onDeleteSuccess()
                    showDeleteDialog = false
                }) { Text("Sil", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Vazgeç") }
            }
        )
    }

    if (showEditSheet) {
        EditGoalBottomSheet(
            goal = goal,
            onDismiss = { showEditSheet = false },
            onConfirm = { updatedGoal ->
                viewModel.updateGoal(updatedGoal)
                showEditSheet = false
            }
        )
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F8F5)),
        title = { Text(text = "Hedef Detayı", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
        },
        actions = {
            IconButton(onClick = { showEditSheet = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color.Black)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
            }
        }
    )
}

@Composable
fun GoalDetailContent(
    goal: SMARTGoalEntity,
    viewModel: GoalDetailViewModel,
    weeklyProgress: List<DailyProgressEntity>,
    dailyValue: Double,
    completedDaysCount: Int,
    modifier: Modifier = Modifier
) {
    val primaryYellow = Color(0xFFFDD835)
    val progress = goal.progressPercent.toFloat() / 100f

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = goal.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Surface(
            color = Color.Black.copy(alpha = 0.05f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = goal.category.uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                strokeWidth = 12.dp,
                modifier = Modifier.size(180.dp),
                color = primaryYellow,
                trackColor = Color.Black.copy(alpha = 0.05f),
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%${goal.progressPercent}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
                Text(text = "Toplam", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bugün: ${dailyValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit ?: ""}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        DayStatisticsSection(goal, completedDaysCount)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Haftalık Performans",
            modifier = Modifier.align(Alignment.Start),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        WeeklyProgressSection(weeklyProgress = weeklyProgress, target = goal.targetValue)

        Spacer(modifier = Modifier.height(24.dp))

        GoalNoteCard(note = goal.note)

        Spacer(modifier = Modifier.height(32.dp))

        val isFutureDate = false // Dashboard'da seçilen tarih varsa kontrol buraya gelir

        if (goal.measurementType == "Sayisal") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.updateProgress(1.0) },
                    enabled = dailyValue < goal.targetValue,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = primaryYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Miktarı Artır", color = primaryYellow, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = { viewModel.updateProgress(1.0) },
                enabled = dailyValue < 1.0,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dailyValue >= 1.0) Color(0xFF4CAF50) else Color.Black
                )
            ) {
                if (dailyValue >= 1.0) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tamamlandı", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Text("Bugünü Tamamla", color = primaryYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}


@Composable
fun DayStatisticsSection(goal: SMARTGoalEntity, completedDaysCount: Int) {
    val totalDays = ((goal.endDate - goal.startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
    val remainingDays = (totalDays - completedDaysCount).coerceAtLeast(0)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Tamamlanan", completedDaysCount, "Gün", Color(0xFFFDD835), Modifier.weight(1f))
        StatCard("Toplam", totalDays, "Gün", Color(0xFF475569), Modifier.weight(1f))
        StatCard("Kalan", remainingDays, "Gün", Color(0xFFEF4444), Modifier.weight(1f))
    }
}

@Composable
fun WeeklyProgressSection(
    weeklyProgress: List<DailyProgressEntity>,
    target: Double
) {
    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(7) { index ->
            val timestamp = calendar.timeInMillis
            val dayData = weeklyProgress.find { it.date == timestamp }
            val isDone = dayData != null && dayData.value >= target

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = if (isDone) Color(0xFF4CAF50) else Color.Black.copy(alpha = 0.05f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isDone) Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp), tint = Color.White)
                        else Text(days[index], fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}

@Composable
fun GoalNoteCard(note: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDD835).copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFDD835))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Notum", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = if (note.isNullOrBlank()) "Henüz not yok." else note, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: Int, unit: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "$value", fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalBottomSheet(
    goal: SMARTGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (SMARTGoalEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    var editedTitle by remember { mutableStateOf(goal.title) }
    var editedTargetValue by remember { mutableStateOf(goal.targetValue.toString()) }
    var isReminderActive by remember { mutableStateOf(goal.isReminderActive) }
    var selectedHour by remember { mutableStateOf(goal.reminderHour ?: 12) }
    var selectedMinute by remember { mutableStateOf(goal.reminderMinute ?: 0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Hedefi Düzenle", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editedTitle,
                onValueChange = { editedTitle = it },
                label = { Text("Hedef Başlığı") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedTargetValue,
                onValueChange = { editedTargetValue = it },
                label = { Text("Hedef Değer") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hatırlatıcı Aktif", fontWeight = FontWeight.Medium)
                Switch(
                    checked = isReminderActive,
                    onCheckedChange = { isReminderActive = it }
                )
            }

            if (isReminderActive) {
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedHour = hour
                                selectedMinute = minute
                            },
                            selectedHour,
                            selectedMinute,
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Saat Seç: ${String.format("%02d:%02d", selectedHour, selectedMinute)}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val updated = goal.copy(
                        title = editedTitle,
                        targetValue = editedTargetValue.toDoubleOrNull() ?: goal.targetValue,
                        isReminderActive = isReminderActive,
                        reminderHour = if (isReminderActive) selectedHour else null,
                        reminderMinute = if (isReminderActive) selectedMinute else null
                    )
                    onConfirm(updated)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Değişiklikleri Kaydet")
            }
        }
    }
}















