package com.esra.goaltrackproject.presentation.addgoal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.utils.toDateString
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onGoalSaved: () -> Unit,
    viewModel: AddGoalViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Adet") }
    var measurementType by remember { mutableStateOf("Sayisal") }
    var note by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    var endDate by remember { mutableStateOf(calendar.timeInMillis) }

    var isReminderActive by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted && isReminderActive) {

        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddGoalUiEvent.Success -> onGoalSaved()
                is AddGoalUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            endDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, min ->
            reminderHour = hour
            reminderMinute = min
        },
        reminderHour, reminderMinute, true
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Yeni Hedef") },
                navigationIcon = {
                    IconButton(onClick = onGoalSaved) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Hedefini Belirle",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Örn: Kitap okuma, Su içme") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFDD835))
            )

            Text(
                text = "Ölçüm Türü",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(24.dp))
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MeasurementTypeSelector("Sayısal", measurementType == "Sayisal") {
                    measurementType = "Sayisal"
                }
                MeasurementTypeSelector(
                    "Tamamlanma",
                    measurementType == "Tamamlanma"
                ) { measurementType = "Tamamlanma" }
            }

            if (measurementType == "Sayisal") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                                targetValue = newValue.replace(",", ".")
                            }
                        },
                        label = { Text("HEDEF DEĞER") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("BİRİM") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                "Not Ekle",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Hedefinle ilgili küçük bir not...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                "Zamanlama",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            DatePickerField(
                "Hedef Bitiş Tarihi",
                endDate.toDateString()
            ) { datePickerDialog.show() }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Günlük Hatırlatma", fontWeight = FontWeight.SemiBold)
                    Text("Her gün aynı saatte", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isReminderActive, onCheckedChange = { isReminderActive = it })
            }

            if (isReminderActive) {
                TimePickerField(
                    "Hatırlatma Saati",
                    reminderHour,
                    reminderMinute
                ) { timePickerDialog.show() }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.addGoal(
                        title = title,
                        description = description,
                        measurementType = measurementType,
                        unit = if (measurementType == "Sayisal") unit else null,
                        targetValue = targetValue.toDoubleOrNull() ?: 1.0,
                        endDate = endDate,
                        isReminderActive = isReminderActive,
                        reminderHour = if (isReminderActive) reminderHour else null,
                        reminderMinute = if (isReminderActive) reminderMinute else null,
                        note = note
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDD835)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hedefi Oluştur", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RowScope.MeasurementTypeSelector(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFFDD835) else Color.Transparent
    val textColor = if (isSelected) Color.Black else Color.Gray
    Box(
        modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DatePickerField(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Text(value, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun TimePickerField(label: String, hour: Int, minute: Int, onClick: () -> Unit) {
    val timeString = String.format("%02d:%02d", hour, minute)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Text(timeString, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}











