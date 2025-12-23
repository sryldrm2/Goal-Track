package com.esra.goaltrackproject.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.data.NotificationEntity
import com.esra.goaltrackproject.data.SMARTGoalEntity
import com.esra.goaltrackproject.data.UserPreferences
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
    onAddGoalClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val userName = remember { userPreferences.getUserName() }
    val avatarResId = remember { userPreferences.getAvatar(com.esra.goaltrackproject.R.drawable.avatar1) }
    val state by viewModel.uiState.collectAsState()
    var showNotificationSheet by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = screenWidth > screenHeight
    val spacing = 8.dp
    val itemWidth = (screenWidth - 40.dp - (spacing * 4)) / 5

    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            containerColor = colorScheme.surface
        ) {
            NotificationSheetContent(
                notifications = state.notifications,
                onMarkAsRead = { viewModel.markNotificationAsRead(it) },
                onDelete = { viewModel.deleteNotification(it) },
                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                onClearAll = { viewModel.clearAllNotifications() }
            )
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGoalClick,
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", modifier = Modifier.size(30.dp))
            }
        }
    ) { padding ->
        if (isLandscape) {
            // YATAY MOD
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = avatarResId),
                                contentDescription = "Profil Resmi",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray.copy(0.3f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Merhaba, $userName", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = colorScheme.onBackground
                            )
                        }
                        Box {
                            IconButton(onClick = { showNotificationSheet = true }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Bildirimler",
                                    modifier = Modifier.size(28.dp),
                                    tint = colorScheme.onBackground
                                )
                            }
                            if (state.unreadNotificationCount > 0) {
                                Surface(
                                    color = Color.Red,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp),
                                    border = BorderStroke(2.dp, colorScheme.surface)
                                ) {
                                    Text(
                                        text = if (state.unreadNotificationCount > 9) "9+" else state.unreadNotificationCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    HeroSuccessCard(totalProgress = state.totalProgressPercent)

                    Surface(
                        color = colorScheme.secondary,
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = state.headerTitle,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colorScheme.onSecondary
                        )
                    }

                    val weekDays = remember { getCurrentWeek() }
                    val listState = rememberLazyListState()
                    val selectedIndex = weekDays.indexOfFirst {
                        isSameDay(it.dateInMillis, state.selectedDate)
                    }
                    val todayIndex = weekDays.indexOfFirst { it.isToday }

                    LaunchedEffect(todayIndex) {
                        if (selectedIndex != -1) {
                            listState.animateScrollToItem(selectedIndex)
                        }
                    }

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
                                primary = colorScheme.secondary,
                                onClick = { viewModel.onDateSelected(day.dateInMillis) }
                            )
                        }
                    }

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        state.categories.forEach { (label, icon) ->
                            CategoryChip(
                                label = label,
                                icon = icon,
                                isSelected = state.selectedCategory == label,
                                primary = colorScheme.secondary,
                                onClick = { viewModel.onCategorySelected(label) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
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
                }
            }
        } else {
            // DİKEY MOD
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = avatarResId),
                                contentDescription = "Profil Resmi",
                                modifier = Modifier
                                    .size(55.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray.copy(0.3f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Merhaba, $userName ", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Ayarlar",
                                modifier = Modifier.size(26.dp),
                                tint = colorScheme.onBackground
                            )
                        }
                        Box {
                            IconButton(onClick = { showNotificationSheet = true }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Bildirimler",
                                    modifier = Modifier.size(28.dp),
                                    tint = colorScheme.onBackground
                                )
                            }
                            if (state.unreadNotificationCount > 0) {
                                Surface(
                                    color = Color.Red,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp),
                                    border = BorderStroke(2.dp, colorScheme.surface)
                                ) {
                                    Text(
                                        text = if (state.unreadNotificationCount > 9) "9+" else state.unreadNotificationCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Başarı Kartı (Hero Card)
                item { HeroSuccessCard(totalProgress = state.totalProgressPercent) }

                // 3. KAYDIRILABİLİR TARİH ŞERİDİ
                item {
                    val weekDays = remember { getCurrentWeek() }
                    val listState = rememberLazyListState()

                    val selectedIndex = weekDays.indexOfFirst {
                        isSameDay(it.dateInMillis, state.selectedDate)
                    }

                    val todayIndex = weekDays.indexOfFirst { it.isToday }

                    LaunchedEffect(todayIndex) {
                        if (selectedIndex != -1) {
                            listState.animateScrollToItem(selectedIndex)
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = colorScheme.secondary,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = state.headerTitle,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = colorScheme.onSecondary
                            )
                        }

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
                                    primary = colorScheme.secondary,
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
                                primary = colorScheme.secondary,
                                onClick = { viewModel.onCategorySelected(label) }
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
}

fun getCurrentWeek(): List<CalendarDay> {
    val calendar = Calendar.getInstance()
    // 7 gün öncesinden başlatmak için
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
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp).clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) primary else colorScheme.surface,
            border = if (!isSelected) BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.2f)) else null,
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) colorScheme.onSecondary else colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = num, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) colorScheme.onSecondary else colorScheme.onSurface)
            }
        }

        // Seçili günün altındaki nokta
        if (isSelected) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(6.dp).background(primary, CircleShape))
        }
    }
}

@Composable
fun CategoryChip(label: String, icon: ImageVector, isSelected: Boolean, primary: Color, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colorScheme.primary else colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.height(45.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isSelected) primary else colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Bold, color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface)
        }
    }
}

@Composable
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Genel" -> Icons.Default.GridView
        "Sağlık" -> Icons.Default.Favorite
        "İş" -> Icons.Default.Work
        "Eğitim" -> Icons.Default.School
        "Spor" -> Icons.Default.FitnessCenter
        else -> Icons.Default.GridView
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
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val isFutureDate = selectedDate > todayStart

    val displayValue = minOf(dailyValue, goal.targetValue)
    val progressFactor = (dailyValue / goal.targetValue).coerceIn(0.0, 1.0)
    val displayPercentage = (progressFactor * 100).toInt()
    val colorScheme = MaterialTheme.colorScheme
    val categoryIcon = getCategoryIcon(goal.category)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.background,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(categoryIcon, contentDescription = null, modifier = Modifier.padding(12.dp), tint = colorScheme.primary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface)
                    Surface(color = colorScheme.background, shape = RoundedCornerShape(4.dp)) {
                        Text(goal.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

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
                        Icon(
                            imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                            contentDescription = if (isDoneToday) "Tamamlandı" else "Artır",
                            tint = if (isDoneToday) Color(0xFF4CAF50) else colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isDoneToday) Color(0xFF4CAF50) else colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (goal.measurementType == "Sayisal") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Günlük: ${displayValue.toInt()}/${goal.targetValue.toInt()} ${goal.unit ?: ""}",
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "%$displayPercentage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressFactor.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = colorScheme.secondary,
                    trackColor = colorScheme.background
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
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = colorScheme.background,
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.padding(20.dp).size(32.dp),
                tint = colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (category == "Tümü") "Henüz bir hedefin yok" else "$category kategorisinde hedef yok",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = colorScheme.onSurface
        )

        Text(
            text = "Hadi, hemen bir tane oluşturmaya başla!",
            fontSize = 13.sp,
            color = colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(45.dp)
        ) {
            Text("İlk Hedefini Ekle", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HeroSuccessCard(totalProgress: Int) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Günlük Başarı", color = colorScheme.onPrimary.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                Text(
                    text = "%$totalProgress",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onPrimary
                )
                Text("Tamamlandı", color = colorScheme.onPrimary.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { totalProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = colorScheme.secondary,
                    trackColor = colorScheme.onPrimary.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

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