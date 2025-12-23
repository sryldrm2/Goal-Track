package com.esra.goaltrackproject.presentation.addgoal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.utils.toDateString
import com.esra.goaltrackproject.presentation.components.addgoal.CategorySelectionDialog
import com.esra.goaltrackproject.presentation.components.addgoal.DatePickerRow
import com.esra.goaltrackproject.presentation.components.addgoal.MonthlySelectionDialog
import com.esra.goaltrackproject.presentation.components.addgoal.SegmentedControl
import com.esra.goaltrackproject.presentation.components.addgoal.TypeSelectionCard
import com.esra.goaltrackproject.presentation.components.addgoal.WeeklySelectionDialog
import com.esra.goaltrackproject.presentation.components.common.FormCard
import com.esra.goaltrackproject.presentation.components.common.ModernTextField
import com.esra.goaltrackproject.presentation.components.common.SectionHeader
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onGoalSaved: () -> Unit,
    viewModel: AddGoalViewModel
) {
    val colorScheme = MaterialTheme.colorScheme

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("Adet") }
    var selectedCategory by remember { mutableStateOf("Genel") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var measurementType by remember { mutableStateOf("Tamamlanma") }
    var frequency by remember { mutableStateOf("Gunluk") }
    var selectedWeeklyDays by remember { mutableStateOf(setOf<Int>()) }
    var selectedMonthlyDays by remember { mutableStateOf(setOf<Int>()) }
    var showWeeklyDialog by remember { mutableStateOf(false) }
    var showMonthlyDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.timeInMillis) }
    var endDate by remember { mutableStateOf(calendar.timeInMillis) }
    var isEndDateActive by remember { mutableStateOf(false) }

    var isReminderActive by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddGoalUiEvent.Success -> onGoalSaved()
                is AddGoalUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Yeni Alışkanlık", color = colorScheme.onSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onGoalSaved) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = colorScheme.onSurface)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.addGoal(
                                title = title,
                                description = description,
                                measurementType = measurementType,
                                unit = if (measurementType == "Sayisal") unit else null,
                                category = selectedCategory,
                                targetValue = targetValue.toDoubleOrNull() ?: 1.0,
                                endDate = if (isEndDateActive) endDate else System.currentTimeMillis() + 31536000000L,
                                isReminderActive = isReminderActive,
                                reminderHour = if (isReminderActive) reminderHour else null,
                                reminderMinute = if (isReminderActive) reminderMinute else null,
                                frequency = frequency,
                                selectedDays = if (frequency == "Haftalık") selectedWeeklyDays else selectedMonthlyDays,
                                note = description
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Oluştur", color = colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // YATAY MOD
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. GENEL BİLGİ
                SectionHeader("Genel bilgi", "Gerekli")
                FormCard {
                    // Başlık Input
                    ModernTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Alışkanlık adı",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Başlangıç Tarihi Seçimi
                    DatePickerRow("Başlangıç tarihi", startDate.toDateString()) {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            startDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }

                    // Bitiş Tarihi Switch
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bitiş tarihi ekle", color = colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isEndDateActive,
                            onCheckedChange = { isEndDateActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = colorScheme.secondary)
                        )
                    }

                    if (isEndDateActive) {
                        DatePickerRow("Bitiş tarihi seç", endDate.toDateString()) {
                            DatePickerDialog(context, { _, y, m, d ->
                                calendar.set(y, m, d)
                                endDate = calendar.timeInMillis
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader("Kategori", null)
                Surface(
                    onClick = { showCategoryDialog = true },
                    color = colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(selectedCategory, color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. SIKLIK
                SectionHeader("Sıklık", null)
                FormCard {
                    Text("Sıklık", color = colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                    SegmentedControl(
                        items = listOf("Günlük", "Haftalık", "Aylık"),
                        selectedItem = frequency,
                        onItemSelected = { item ->
                            frequency = item
                            if (item == "Haftalık") showWeeklyDialog = true
                            if (item == "Aylık") showMonthlyDialog = true
                        }
                    )

                    if (frequency != "Gunluk") {
                        val count = if (frequency == "Haftalık") selectedWeeklyDays.size else selectedMonthlyDays.size
                        Text(
                            text = "$count gün seçildi - Düzenlemek için tıkla",
                            fontSize = 12.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp)
                                .clickable { if (frequency == "Haftalık") showWeeklyDialog = true else showMonthlyDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. DEĞERLENDİRME TÜRÜ
                SectionHeader("Değerlendirme türü", null)

                TypeSelectionCard(
                    "EVET VEYA HAYIR",
                    "Sadece aktiviteyi yapıp yapmadığınızı takip etmek mi istiyorsunuz?",
                    measurementType == "Tamamlanma"
                ) { measurementType = "Tamamlanma" }

                Spacer(modifier = Modifier.height(12.dp))

                TypeSelectionCard(
                    "SAYISAL DEĞER",
                    "Günlük hedef veya limit belirlemek ister misiniz?",
                    measurementType == "Sayisal"
                ) { measurementType = "Sayisal" }

                if (measurementType == "Sayisal") {
                    FormCard(Modifier.padding(top = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ModernTextField(
                                value = targetValue,
                                onValueChange = { targetValue = it },
                                placeholder = "Hedef",
                                label = "Hedef",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number
                            )
                            ModernTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                placeholder = "Birim",
                                label = "Birim",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. HATIRLATICI & NOTLAR
                SectionHeader("Hatırlatıcı", "İsteğe Bağlı")
                FormCard {
                    Surface(
                        onClick = {
                            isReminderActive = true
                            TimePickerDialog(context, { _, h, m ->
                                reminderHour = h
                                reminderMinute = m
                            }, reminderHour, reminderMinute, true).show()
                        },
                        color = colorScheme.background,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = null, tint = colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (isReminderActive) "Hergün ${String.format("%02d:%02d", reminderHour, reminderMinute)}"
                                else "Hatırlatıcı ekle",
                                color = colorScheme.onSurface, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader("Diger", "İsteğe Bağlı")
                FormCard {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Açıklama veya motivasyon notu...", color = colorScheme.onSurface.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
                            cursorColor = colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        } else {
            // DİKEY MOD
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. GENEL BİLGİ
                SectionHeader("Genel bilgi", "Gerekli")
                FormCard {
                    // Başlık Input
                    ModernTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Alışkanlık adı",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Başlangıç Tarihi Seçimi
                    DatePickerRow("Başlangıç tarihi", startDate.toDateString()) {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            startDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }

                    // Bitiş Tarihi Switch
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bitiş tarihi ekle", color = colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isEndDateActive,
                            onCheckedChange = { isEndDateActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = colorScheme.secondary)
                        )
                    }

                    if (isEndDateActive) {
                        DatePickerRow("Bitiş tarihi seç", endDate.toDateString()) {
                            DatePickerDialog(context, { _, y, m, d ->
                                calendar.set(y, m, d)
                                endDate = calendar.timeInMillis
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader("Kategori", null)
                Surface(
                    onClick = { showCategoryDialog = true },
                    color = colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(selectedCategory, color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. SIKLIK
                SectionHeader("Sıklık", null)
                FormCard {
                    Text("Sıklık", color = colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                    SegmentedControl(
                        items = listOf("Günlük", "Haftalık", "Aylık"),
                        selectedItem = frequency,
                        onItemSelected = { item ->
                            frequency = item
                            if (item == "Haftalık") showWeeklyDialog = true
                            if (item == "Aylık") showMonthlyDialog = true
                        }
                    )

                    if (frequency != "Gunluk") {
                        val count = if (frequency == "Haftalık") selectedWeeklyDays.size else selectedMonthlyDays.size
                        Text(
                            text = "$count gün seçildi - Düzenlemek için tıkla",
                            fontSize = 12.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp)
                                .clickable { if (frequency == "Haftalık") showWeeklyDialog = true else showMonthlyDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. DEĞERLENDİRME TÜRÜ
                SectionHeader("Değerlendirme türü", null)

                TypeSelectionCard(
                    "EVET VEYA HAYIR",
                    "Sadece aktiviteyi yapıp yapmadığınızı takip etmek mi istiyorsunuz?",
                    measurementType == "Tamamlanma"
                ) { measurementType = "Tamamlanma" }

                Spacer(modifier = Modifier.height(12.dp))

                TypeSelectionCard(
                    "SAYISAL DEĞER",
                    "Günlük hedef veya limit belirlemek ister misiniz?",
                    measurementType == "Sayisal"
                ) { measurementType = "Sayisal" }

                if (measurementType == "Sayisal") {
                    FormCard(Modifier.padding(top = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ModernTextField(
                                value = targetValue,
                                onValueChange = { targetValue = it },
                                placeholder = "Hedef",
                                label = "Hedef",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number
                            )
                            ModernTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                placeholder = "Birim",
                                label = "Birim",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. HATIRLATICI & NOTLAR
                SectionHeader("Hatırlatıcı", "İsteğe Bağlı")
                FormCard {
                    Surface(
                        onClick = {
                            isReminderActive = true
                            TimePickerDialog(context, { _, h, m ->
                                reminderHour = h
                                reminderMinute = m
                            }, reminderHour, reminderMinute, true).show()
                        },
                        color = colorScheme.background,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = null, tint = colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (isReminderActive) "Hergün ${String.format("%02d:%02d", reminderHour, reminderMinute)}"
                                else "Hatırlatıcı ekle",
                                color = colorScheme.onSurface, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader("Diger", "İsteğe Bağlı")
                FormCard {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Açıklama veya motivasyon notu...", color = colorScheme.onSurface.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
                            cursorColor = colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    if (showCategoryDialog) {
        CategorySelectionDialog(
            selectedCategory = selectedCategory,
            onCategorySelected = { categoryName ->
                selectedCategory = categoryName
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
    if (showWeeklyDialog) {
        WeeklySelectionDialog(
            selectedDays = selectedWeeklyDays,
            onDayClick = { day ->
                selectedWeeklyDays = if (selectedWeeklyDays.contains(day)) {
                    selectedWeeklyDays - day
                } else {
                    selectedWeeklyDays + day
                }
            },
            onDismiss = { showWeeklyDialog = false }
        )
    }

    if (showMonthlyDialog) {
        MonthlySelectionDialog(
            selectedDays = selectedMonthlyDays,
            onDayClick = { day ->
                selectedMonthlyDays = if (selectedMonthlyDays.contains(day)) {
                    selectedMonthlyDays - day
                } else {
                    selectedMonthlyDays + day
                }
            },
            onDismiss = { showMonthlyDialog = false }
        )
    }

}






