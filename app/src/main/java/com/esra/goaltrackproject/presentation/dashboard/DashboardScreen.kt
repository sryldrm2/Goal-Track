package com.esra.goaltrackproject.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.data.UserPreferences
import com.esra.goaltrackproject.presentation.components.common.EmptyStateComponent
import com.esra.goaltrackproject.presentation.components.dashboard.CategoryChip
import com.esra.goaltrackproject.presentation.components.dashboard.DateItem
import com.esra.goaltrackproject.presentation.components.dashboard.GoalListItem
import com.esra.goaltrackproject.presentation.components.dashboard.HeroSuccessCard
import com.esra.goaltrackproject.presentation.components.dashboard.NotificationSheetContent
import com.esra.goaltrackproject.utils.getCurrentWeek
import com.esra.goaltrackproject.utils.isSameDay

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
                                "Merhaba, $userName", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = colorScheme.onBackground
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                }

                // Başarı Kartı (Hero Card)
                item { HeroSuccessCard(totalProgress = state.totalProgressPercent) }

                // KAYDIRILABİLİR TARİH ŞERİDİ
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

                // Kategori Filtreleri
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

                // Hedef Listesi
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




