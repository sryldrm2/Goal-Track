package com.esra.goaltrackproject.presentation.dashboard

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.data.SMARTGoalEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarDay(
    val dayName: String,
    val dayNumber: String,
    val dateInMillis: Long,
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onGoalClick: (Int) -> Unit,
    onAddGoalClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val backgroundLight = Color(0xFFF8F8F5)
    val primaryYellow = Color(0xFFFDD835)

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val spacing = 8.dp
    val itemWidth = (screenWidth - 40.dp - (spacing * 4)) / 5

    Scaffold(
        containerColor = backgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGoalClick,
                containerColor = Color.Black,
                contentColor = primaryYellow,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", modifier = Modifier.size(30.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Header (Selamlama)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, modifier = Modifier.size(50.dp), color = Color.LightGray) {}
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Merhaba, Esra 👋", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text("Bugünün Hedefleri", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = {
                        Toast.makeText(
                            context,
                            "Bildirim ayarları çok yakında!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp))
                    }
                }
            }

            // 2. Başarı Kartı (Hero Card)
            item { HeroSuccessCard(totalProgress = state.totalProgressPercent) }

            // --- 3. ADIM: 5 GÜNLÜK KAYDIRILABİLİR TARİH ŞERİDİ ---
            item {
                val weekDays = remember { getCurrentWeek() }
                val listState = rememberLazyListState()

                val selectedIndex = weekDays.indexOfFirst {
                    isSameDay(it.dateInMillis, state.selectedDate)
                }

                val todayIndex = weekDays.indexOfFirst { it.isToday }

                // Sadece scroll ile ortalamayı yap, padding hesaplaması yok
                LaunchedEffect(todayIndex) {
                    if (selectedIndex != -1) {
                        listState.animateScrollToItem(selectedIndex)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // "Bugün" Başlığı - Tam ortalanmış
                    Surface(
                        color = Color(0xFFFDD835),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = state.headerTitle,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    // KAYDIRILABİLİR ŞERİT
                    LazyRow(
                        state = listState,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                    ) {
                        items(weekDays) { day ->
                            DateItem(
                                day = day.dayName,
                                num = day.dayNumber,
                                isSelected = isSameDay(day.dateInMillis, state.selectedDate),
                                primary = Color(0xFFFDD835),
                                onClick = { viewModel.onDateSelected(day.dateInMillis) }
                            )
                        }
                    }
                }
            }

            // 4. Kategori Filtreleri
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.categories.forEach { (label, icon) ->
                        CategoryChip(
                            label = label,
                            icon = icon,
                            isSelected = state.selectedCategory == label,
                            primary = primaryYellow,
                            onClick = { viewModel.onCategorySelected(label) } // ✅ Tıklama eklendi
                        )
                    }
                }
            }
            // 5. Hedef Listesi
            if (state.todayGoals.isEmpty()) {
                item {
                    EmptyStateComponent(
                        category = state.selectedCategory,
                        onAddClick = onAddGoalClick
                    )
                }
            } else {
                items(state.todayGoals) { goal ->
                    GoalListItem(
                        goal = goal,
                        isDoneToday = state.dailyCompletionMap[goal.id] ?: false,
                        dailyValue = state.dailyValueMap[goal.id] ?: 0.0,
                        selectedDate = state.selectedDate,
                        onNavigate = { onGoalClick(goal.id) },
                        onIncrement = { viewModel.incrementProgress(goal) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

fun getCurrentWeek(): List<CalendarDay> {
    val calendar = Calendar.getInstance()
    // 7 gün öncesinden başlatıyoruz
    calendar.add(Calendar.DAY_OF_YEAR, -7)

    val sdfDayName = SimpleDateFormat("EEE", Locale("tr"))
    val sdfDayNum = SimpleDateFormat("dd", Locale.getDefault())

    return (0..14).map {
        val dateInMillis = calendar.timeInMillis
        val dayName = sdfDayName.format(calendar.time).uppercase()
        val dayNum = sdfDayNum.format(calendar.time)
        val isToday = isSameDay(dateInMillis, System.currentTimeMillis())
        val result = CalendarDay(
            dayName = dayName,
            dayNumber = dayNum,
            dateInMillis = dateInMillis,
            isToday = isToday
        )
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        result
    }
}

fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun DateItem(
    day: String,
    num: String,
    isSelected: Boolean,
    primary: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp).clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) primary else Color.White,
            border = if (!isSelected) BorderStroke(1.dp, Color.LightGray.copy(0.3f)) else null,
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = num, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }
        }

        // Seçili günün altındaki o ikonik nokta
        if (isSelected) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(6.dp).background(Color(0xFF3F51B5), CircleShape))
        }
    }
}

@Composable
fun CategoryChip(label: String, icon: ImageVector, isSelected: Boolean, primary: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.Black else Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.height(45.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isSelected) primary else Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
        }
    }
}

@Composable
fun GoalListItem(
    goal: SMARTGoalEntity,
    isDoneToday: Boolean,
    dailyValue: Double,
    selectedDate: Long,
    onNavigate: () -> Unit,
    onIncrement: () -> Unit
) {
    // Gelecek günler için kontrol
    val todayStart = java.util.Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val isFutureDate = selectedDate > todayStart

    val displayValue = minOf(dailyValue, goal.targetValue)
    val progressFactor = (dailyValue / goal.targetValue).coerceIn(0.0, 1.0)
    val displayPercentage = (progressFactor * 100).toInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // İkon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F8F5),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color(0xFFE57373))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Başlık ve Kategori
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(color = Color(0xFFF2F2ED), shape = RoundedCornerShape(4.dp)) {
                        Text(goal.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.Gray)
                    }
                }

                // Aksiyon Butonu
                IconButton(
                    onClick = onIncrement,
                    enabled = !isFutureDate // Gelecek günler için devre dışı
                ) {
                    if (isFutureDate) {
                        // Gelecek günler için: Gri kilit ikonu
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Gelecek gün",
                            tint = Color.LightGray,
                            modifier = Modifier.size(28.dp)
                        )
                    } else if (goal.measurementType == "Sayisal") {
                        // Sayısal hedefler için: Günlük hedefe ulaşıldıysa yeşil tik, değilse artırma butonu
                        Icon(
                            imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                            contentDescription = if (isDoneToday) "Tamamlandı" else "Artır",
                            tint = if (isDoneToday) Color(0xFF4CAF50) else Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        // Tamamlanma hedefleri için: Mevcut mantık
                        Icon(
                            imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isDoneToday) Color(0xFF4CAF50) else Color.LightGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Sayısal Hedef İlerleme Çubuğu
            if (goal.measurementType == "Sayisal") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ✅ Düzeltilmiş Değer: displayValue kullanıldı
                    Text(
                        text = "Günlük: ${displayValue.toInt()}/${goal.targetValue.toInt()} ${goal.unit ?: ""}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    // ✅ Düzeltilmiş Yüzde: displayPercentage kullanıldı
                    Text(
                        text = "%$displayPercentage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // ✅ Düzeltilmiş Çubuk: progressFactor kullanıldı
                LinearProgressIndicator(
                    progress = { progressFactor.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(0xFFFDD835),
                    trackColor = Color(0xFFF2F2ED)
                )
            }
        }
    }
}

@Composable
fun EmptyStateComponent(
    category: String,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color(0xFFF2F2ED),
            shape = CircleShape,
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.padding(24.dp).size(40.dp),
                tint = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (category == "Tümü") "Henüz bir hedefin yok" else "$category kategorisinde hedef yok",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )

        Text(
            text = "Hadi, hemen bir tane oluşturmaya başla!",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("İlk Hedefini Ekle", color = Color.White)
        }
    }
}

@Composable
fun HeroSuccessCard(totalProgress: Int) {
    val primaryYellow = Color(0xFFFDD835)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = primaryYellow)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Günlük Başarı", color = Color.Black.copy(0.7f), fontWeight = FontWeight.Medium)
                Text(
                    text = "%$totalProgress",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text("Tamamlandı", color = Color.Black.copy(0.7f))
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { totalProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color.Black,
                    trackColor = Color.Black.copy(0.1f)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(0.1f),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

