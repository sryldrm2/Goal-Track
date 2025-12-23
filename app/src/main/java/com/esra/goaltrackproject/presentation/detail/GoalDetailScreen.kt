package com.esra.goaltrackproject.presentation.detail

import android.app.TimePickerDialog
import android.widget.Toast
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
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
    val totalDays by viewModel.totalDays.collectAsState()
    val remainingDays by viewModel.remainingDays.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    if (goal == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorScheme.secondary)
        }
        return
    }

    Scaffold(
        containerColor = colorScheme.background,
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
            totalDays = totalDays,
            remainingDays = remainingDays,
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

    val colorScheme = MaterialTheme.colorScheme
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background),
        title = { Text(text = "Hedef Detayı", fontWeight = FontWeight.Bold, color = colorScheme.onBackground) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = { showEditSheet = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = colorScheme.onBackground)
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
    totalDays: Int,
    remainingDays: Int,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val colorScheme = MaterialTheme.colorScheme
    val progress = goal.progressPercent.toFloat() / 100f

    if (isLandscape) {
        // YATAY MOD
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = goal.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = colorScheme.onBackground.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = goal.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        strokeWidth = 10.dp,
                        modifier = Modifier.size(140.dp),
                        color = colorScheme.secondary,
                        trackColor = colorScheme.onBackground.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(text = "%${goal.progressPercent}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (goal.measurementType == "Sayisal") {
                        "Bugün: ${dailyValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit ?: ""}"
                    } else {
                        if (dailyValue >= 1.0) "Bugün: Tamamlandı ✅" else "Bugün: Henüz Yapılmadı"
                    },
                    color = colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                DayStatisticsSection(
                    goal = goal,
                    completedDaysCount = completedDaysCount,
                    totalDays = totalDays,
                    remainingDays = remainingDays
                    )
                Text(
                    "Haftalık Performans",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                WeeklyProgressSection(weeklyProgress = weeklyProgress, target = goal.targetValue)
                GoalNoteCard(note = goal.note)

                DetailActionButton(goal, dailyValue, viewModel)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        // DİKEY MOD
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = goal.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = colorScheme.onBackground)
            Surface(
                color = colorScheme.onBackground.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = goal.category.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 12.dp,
                    modifier = Modifier.size(180.dp),
                    color = colorScheme.secondary,
                    trackColor = colorScheme.onBackground.copy(alpha = 0.05f),
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%${goal.progressPercent}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onBackground
                    )
                    Text(text = "Toplam", fontSize = 14.sp, color = colorScheme.onBackground.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (goal.measurementType == "Sayisal") {
                    "Bugün: ${dailyValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit ?: ""}"
                } else {
                    if (dailyValue >= 1.0) "Bugün: Tamamlandı ✅" else "Bugün: Henüz Yapılmadı"
                },
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            DayStatisticsSection(
                goal = goal,
                completedDaysCount = completedDaysCount,
                totalDays = totalDays,
                remainingDays = remainingDays
            )

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

            DetailActionButton(goal, dailyValue, viewModel)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailActionButton(goal: SMARTGoalEntity, dailyValue: Double, viewModel: GoalDetailViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    if (goal.measurementType == "Sayisal") {
        Button(
            onClick = { viewModel.updateProgress(1.0) },
            enabled = dailyValue < goal.targetValue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Miktarı Artır", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    } else {
        Button(
            onClick = { viewModel.updateProgress(1.0) },
            enabled = dailyValue < 1.0,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dailyValue >= 1.0) Color(0xFF4CAF50) else colorScheme.primary
            )
        ) {
            if (dailyValue >= 1.0) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tamamlandı", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Text("Bugünü Tamamla", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun DayStatisticsSection(
    goal: SMARTGoalEntity,
    completedDaysCount: Int,
    totalDays: Int,
    remainingDays: Int
) {

    val colorScheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Tamamlanan", completedDaysCount, "Gün", colorScheme.secondary, Modifier.weight(1f))
        StatCard("Toplam", totalDays, "Gün", colorScheme.primary, Modifier.weight(1f))
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

            val colorScheme = MaterialTheme.colorScheme
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = if (isDone) Color(0xFF4CAF50) else colorScheme.onBackground.copy(alpha = 0.05f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isDone) Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp), tint = Color.White)
                        else Text(days[index], fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                    }
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}

@Composable
fun GoalNoteCard(note: String?) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.secondary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Notum", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colorScheme.onSurface)
                Text(text = if (note.isNullOrBlank()) "Henüz not yok." else note, fontSize = 13.sp, color = colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: Int, unit: String, color: Color, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "$value", fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface.copy(alpha = 0.6f))
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

    val colorScheme = MaterialTheme.colorScheme
    var editedTitle by remember { mutableStateOf(goal.title) }
    var editedTargetValue by remember { mutableStateOf(goal.targetValue.toString()) }
    var editedUnit by remember { mutableStateOf(goal.unit ?: "") }
    var editedDescription by remember { mutableStateOf(goal.note ?: "") }
    var isReminderActive by remember { mutableStateOf(goal.isReminderActive) }
    var selectedHour by remember { mutableStateOf(goal.reminderHour ?: 12) }
    var selectedMinute by remember { mutableStateOf(goal.reminderMinute ?: 0) }
    val isNumericGoal = goal.measurementType == "Sayisal"

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
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sadece sayısal hedefler için hedef değer ve birim göster
            if (isNumericGoal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedTargetValue,
                        onValueChange = { editedTargetValue = it },
                        label = { Text("Hedef Değer") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )
                    OutlinedTextField(
                        value = editedUnit,
                        onValueChange = { editedUnit = it },
                        label = { Text("Birim") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Açıklama/Not alanı
            OutlinedTextField(
                value = editedDescription,
                onValueChange = { editedDescription = it },
                label = { Text("Açıklama / Not") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
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
                        targetValue = if (isNumericGoal) {
                            editedTargetValue.toDoubleOrNull() ?: goal.targetValue
                        } else {
                            1.0 // Evet/Hayır hedefleri için her zaman 1.0
                        },
                        unit = if (isNumericGoal) editedUnit.ifBlank { null } else goal.unit,
                        note = editedDescription.ifBlank { null },
                        isReminderActive = isReminderActive,
                        reminderHour = if (isReminderActive) selectedHour else null,
                        reminderMinute = if (isReminderActive) selectedMinute else null
                    )
                    onConfirm(updated)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                )
            ) {
                Text("Değişiklikleri Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    }
}















