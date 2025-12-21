package com.esra.goaltrackproject.presentation.addgoal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.utils.toDateString
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
    val primaryYellow = Color(0xFFFDD835)
    val backgroundLight = Color(0xFFF8F8F5)
    val cardWhite = Color(0xFFFFFFFF)
    val inputBg = Color(0xFFF2F2ED)
    val textBlack = Color(0xFF1A1A1A)
    val textGray = Color(0xFF757575)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("Adet") }
    var selectedCategory by remember { mutableStateOf("Genel") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    val categories = listOf("Genel", "Sağlık", "İş", "Eğitim")
    var measurementType by remember { mutableStateOf("Tamamlanma") }
    var frequency by remember { mutableStateOf("Gunluk") }
    var priority by remember { mutableStateOf("Varsayılan") }

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
        containerColor = backgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Yeni Alışkanlık", color = textBlack, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onGoalSaved) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = textBlack)
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
                                note = description
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryYellow),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Oluştur", color = textBlack, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundLight)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- 1. GENEL BİLGİ ---
            SectionHeader("Genel bilgi", "Gerekli", textBlack)
            FormCard(cardWhite) {
                // Başlık Input
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Alışkanlık adı") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Başlangıç Tarihi Seçimi
                DatePickerRow("Başlangıç tarihi", startDate.toDateString(), textBlack, inputBg) {
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
                    Text("Bitiş tarihi ekle", color = textBlack, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isEndDateActive,
                        onCheckedChange = { isEndDateActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = primaryYellow)
                    )
                }

                if (isEndDateActive) {
                    DatePickerRow("Bitiş tarihi seç", endDate.toDateString(), textBlack, inputBg) {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            endDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Kategori", null, textBlack)
            Surface(
                onClick = { showCategoryDialog = true },
                color = inputBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(selectedCategory, color = textBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = textGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. SIKLIK ---
            SectionHeader("Sıklık", null, textBlack)
            FormCard(cardWhite) {
                listOf("Günlük", "Haftalık", "Aylık").forEach { item ->
                    FrequencyOption(item, frequency == item, primaryYellow, textBlack) { frequency = item }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. DEĞERLENDİRME TÜRÜ ---
            SectionHeader("Değerlendirme türü", null, textBlack)

            TypeSelectionCard(
                "EVET VEYA HAYIR",
                "Sadece aktiviteyi yapıp yapmadığınızı takip etmek mi istiyorsunuz?",
                measurementType == "Tamamlanma",
                primaryYellow, cardWhite, textBlack, textGray
            ) { measurementType = "Tamamlanma" }

            Spacer(modifier = Modifier.height(12.dp))

            TypeSelectionCard(
                "SAYISAL DEĞER",
                "Günlük hedef veya limit belirlemek ister misiniz?",
                measurementType == "Sayisal",
                primaryYellow, cardWhite, textBlack, textGray
            ) { measurementType = "Sayisal" }

            if (measurementType == "Sayisal") {
                FormCard(cardWhite, Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = targetValue,
                            onValueChange = { targetValue = it },
                            label = { Text("Hedef") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(focusedContainerColor = inputBg, unfocusedContainerColor = inputBg)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Birim") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedContainerColor = inputBg, unfocusedContainerColor = inputBg)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. HATIRLATICI & NOTLAR ---
            SectionHeader("Hatırlatıcı", "İsteğe Bağlı", textBlack)
            FormCard(cardWhite) {
                Surface(
                    onClick = {
                        isReminderActive = true
                        TimePickerDialog(context, { _, h, m ->
                            reminderHour = h
                            reminderMinute = m
                        }, reminderHour, reminderMinute, true).show()
                    },
                    color = inputBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null, tint = primaryYellow)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isReminderActive) "Hergün ${String.format("%02d:%02d", reminderHour, reminderMinute)}"
                            else "Hatırlatıcı ekle",
                            color = textBlack, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Diger", "İsteğe Bağlı", textBlack)
            FormCard(cardWhite) {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Açıklama veya motivasyon notu...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    if (showCategoryDialog) {
        CategorySelectionDialog(
            onCategorySelected = { categoryName ->
                selectedCategory = categoryName
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelectionDialog(
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val primaryYellow = Color(0xFFFDD835)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("KAPAT", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        },
        title = {
            Text("Kategori Seçin", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        },
        text = {
            Column {
                Divider(color = Color.LightGray.copy(0.3f), thickness = 1.dp)
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
                            primaryColor = primaryYellow
                        ) {
                            onCategorySelected(item.name)
                            onDismiss()
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Composable
fun CategoryDialogItem(
    item: CategoryItem,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F8F5),
        border = BorderStroke(1.dp, Color.LightGray.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = primaryColor,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp),
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, tag: String?, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)) {
        Text(title, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (tag != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(color = Color.Black.copy(0.05f), shape = RoundedCornerShape(4.dp)) {
                Text(tag, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
fun FormCard(bgColor: Color, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
        content = { Column(modifier = Modifier.padding(16.dp), content = content) }
    )
}

@Composable
fun FrequencyOption(text: String, isSelected: Boolean, primary: Color, textBlack: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = primary)
        )
        Text(text, color = textBlack, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun TypeSelectionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    primary: Color,
    cardBg: Color,
    textBlack: Color,
    textGray: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = cardBg,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        border = if (isSelected) BorderStroke(2.dp, primary) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = textBlack, fontWeight = FontWeight.Bold)
                Text(desc, color = textGray, fontSize = 13.sp)
            }
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = primary))
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
fun DatePickerRow(label: String, value: String, textColor: Color, bg: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = textColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = textColor, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun CustomInputLabel(text: String) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
}

@Composable
fun EvaluationOption(title: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier, primary: Color, bg: Color, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) primary.copy(alpha = 0.1f) else bg,
        border = if (isSelected) BorderStroke(2.dp, primary) else null
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = if (isSelected) primary else Color.White, modifier = Modifier.size(36.dp)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
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











