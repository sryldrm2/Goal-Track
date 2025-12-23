package com.esra.goaltrackproject.presentation.addgoal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.utils.toDateString
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar

data class CategoryItem(
    val name: String,
    val icon: ImageVector
)

val categoryList = listOf(
    CategoryItem("Genel", Icons.Default.GridView),
    CategoryItem("Sağlık", Icons.Default.Favorite),
    CategoryItem("İş", Icons.Default.Work),
    CategoryItem("Eğitim", Icons.Default.School),
    CategoryItem("Spor", Icons.Default.FitnessCenter),
)

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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
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

                    Spacer(modifier = Modifier.height(20.dp))

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
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    SectionHeader("Değerlendirme & Sıklık", null)
                    FormCard {
                        // Sıklık
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Değerlendirme Türü
                        Text("Değerlendirme türü", color = colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
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
                            Spacer(modifier = Modifier.height(12.dp))
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

                    Spacer(modifier = Modifier.height(20.dp))

                    SectionHeader("Hatırlatıcı & Notlar", "İsteğe Bağlı")
                    FormCard {
                        // Hatırlatıcı
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Açıklama
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

                // --- 2. SIKLIK ---
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

                // --- 3. DEĞERLENDİRME TÜRÜ ---
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

                // --- 4. HATIRLATICI & NOTLAR ---
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelectionDialog(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("KAPAT", color = colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        },
        title = {
            Text("Kategori Seçin", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = colorScheme.onSurface)
        },
        text = {
            Column {
                Divider(color = colorScheme.onSurface.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 kolon sabit
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(categoryList) { item ->
                        CategoryDialogItem(
                            item = item,
                            isSelected = item.name == selectedCategory
                        ) {
                            onCategorySelected(item.name)
                            onDismiss()
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = colorScheme.surface
    )
}

@Composable
fun CategoryDialogItem(
    item: CategoryItem,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

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

@Composable
fun FormCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth(),
        content = { Column(modifier = Modifier.padding(20.dp), content = content) }
    )
}

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.background,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                Surface(
                    onClick = { onItemSelected(item) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colorScheme.primary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyDayPicker(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text("Hangi günler?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface.copy(alpha = 0.6f))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val isSelected = selectedDays.contains(dayNum)
                Surface(
                    onClick = { onDayClick(dayNum) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) colorScheme.primary else Color.Transparent,
                    border = if (!isSelected) BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.3f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            day, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklySelectionDialog(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tamam", color = colorScheme.primary) } },
        title = { Text("Günleri Seçin", fontWeight = FontWeight.Bold, color = colorScheme.onSurface) },
        text = {
            WeeklyDayPicker(selectedDays, onDayClick)
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = colorScheme.surface
    )
}

@Composable
fun MonthlyDayPicker(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text("Ayın hangi günleri?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface.copy(alpha = 0.6f))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7), // 7li grid
            modifier = Modifier.height(200.dp).padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(31) { index ->
                val dayNum = index + 1
                val isSelected = selectedDays.contains(dayNum)
                Surface(
                    onClick = { onDayClick(dayNum) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) colorScheme.primary else colorScheme.background,
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$dayNum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySelectionDialog(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tamam", color = colorScheme.primary) } },
        title = { Text("Ayın Günlerini Seçin", fontWeight = FontWeight.Bold, color = colorScheme.onSurface) },
        text = {
            MonthlyDayPicker(selectedDays, onDayClick)
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = colorScheme.surface
    )
}

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

@Composable
fun GoalTextField(label: String, value: String, onValueChange: (String) -> Unit, textColor: Color) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedTextColor = textColor,
            focusedTextColor = textColor
        )
    )
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = colorScheme.onSurface.copy(alpha = 0.5f)) },
        label = label?.let { { Text(it, color = colorScheme.onSurface.copy(alpha = 0.7f)) } },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface,
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = colorScheme.onSurface.copy(alpha = 0.7f),
            unfocusedLabelColor = colorScheme.onSurface.copy(alpha = 0.5f),
            cursorColor = colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DatePickerRow(label: String, value: String, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
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
            Column {
                Text(label, color = colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Icon(Icons.Default.DateRange, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun CustomInputLabel(text: String) {
    val colorScheme = MaterialTheme.colorScheme
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
}

@Composable
fun EvaluationOption(title: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier, primary: Color, bg: Color, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) colorScheme.primary.copy(alpha = 0.1f) else colorScheme.surface,
        border = if (isSelected) BorderStroke(2.dp, colorScheme.primary) else null
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = if (isSelected) colorScheme.primary else colorScheme.surface, modifier = Modifier.size(36.dp)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = colorScheme.onSurface)
        }
    }
}

@Composable
fun RowScope.MeasurementTypeSelector(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (isSelected) colorScheme.secondary else Color.Transparent
    val textColor = if (isSelected) colorScheme.onSecondary else colorScheme.onSurface.copy(alpha = 0.6f)
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
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(colorScheme.background, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurface.copy(alpha = 0.6f))
                Text(value, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun TimePickerField(label: String, hour: Int, minute: Int, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val timeString = String.format("%02d:%02d", hour, minute)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(colorScheme.background, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurface.copy(alpha = 0.6f))
                Text(timeString, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface)
            }
        }
    }
}











