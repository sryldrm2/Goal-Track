package com.esra.goaltrackproject.presentation.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.SMARTGoalEntity
import com.esra.goaltrackproject.data.DailyProgressEntity
import com.esra.goaltrackproject.data.NotificationDao
import com.esra.goaltrackproject.data.NotificationEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val activeGoalsCount: Int = 0,
    val completedGoalsCount: Int = 0,
    val nearestGoal: SMARTGoalEntity? = null,
    val totalProgressPercent: Int = 0,
    val todayGoals: List<SMARTGoalEntity> = emptyList(),
    val allGoals: List<SMARTGoalEntity> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val headerTitle: String = "Bugün",
    val dailyCompletionMap: Map<Int, Boolean> = emptyMap(),
    val selectedCategory: String = "Tümü",
    val dailyValueMap: Map<Int, Double> = emptyMap(),
    val categories: List<Pair<String, ImageVector>> = listOf(
        "Tümü" to Icons.Default.GridView,
        "Genel" to Icons.Default.GridView,
        "Sağlık" to Icons.Default.Favorite,
        "İş" to Icons.Default.Work,
        "Eğitim" to Icons.Default.School,
        "Spor" to Icons.Default.FitnessCenter
    ),
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadNotificationCount: Int = 0,
)

class DashboardViewModel(
    private val goalDao: GoalDao,
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Tümü")
    private val _selectedDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        goalDao.countActiveGoals(),
        goalDao.countCompletedGoals(),
        goalDao.getNearestDeadlineGoal(),
        _selectedDate.flatMapLatest { date -> goalDao.getGoalsForDate(date) },
        goalDao.getAllGoals(),
        _selectedDate,
        _selectedCategory,
        notificationDao.getAllNotifications(),
        notificationDao.getUnreadCount()
    ) { args: Array<Any?> ->
        val active = args[0] as Int
        val completed = args[1] as Int
        val nearest = args[2] as SMARTGoalEntity?
        val filteredByDate = args[3] as List<SMARTGoalEntity>
        val actualAllList = args[4] as List<SMARTGoalEntity>
        val selectedDate = args[5] as Long
        val currentCategory = args[6] as String
        val notifications = args[7] as List<NotificationEntity>
        val unreadCount = args[8] as Int

        val frequencyFilteredGoals = filteredByDate.filter { goal ->
            isGoalVisibleOnDate(goal, selectedDate)
        }

        val finalGoals = if (currentCategory == "Tümü") {
            frequencyFilteredGoals
        } else {
            frequencyFilteredGoals.filter { it.category.equals(currentCategory, ignoreCase = true) }
        }

        val valueMap = mutableMapOf<Int, Double>()
        val completionMap = mutableMapOf<Int, Boolean>()

        finalGoals.forEach { goal ->
            val progress = runBlocking {
                goalDao.getDailyProgressForDate(goal.id, selectedDate)
            }
            val valToday = progress?.value ?: 0.0
            valueMap[goal.id] = valToday

            completionMap[goal.id] = if (goal.measurementType == "Sayisal") {
                valToday >= goal.targetValue
            } else {
                valToday > 0
            }
        }

        // Seçilen güne özel günlük progress hesapla
        val dailyProgressPercent = if (finalGoals.isNotEmpty()) {
            val dailyProgresses = finalGoals.map { goal ->
                val dailyValue = valueMap[goal.id] ?: 0.0
                if (goal.measurementType == "Sayisal") {
                    ((dailyValue / goal.targetValue).coerceIn(0.0, 1.0) * 100).toInt()
                } else {
                    if (dailyValue > 0) 100 else 0
                }
            }
            dailyProgresses.average().toInt()
        } else {
            0
        }

        DashboardUiState(
            activeGoalsCount = active,
            completedGoalsCount = completed,
            nearestGoal = nearest,
            totalProgressPercent = dailyProgressPercent,
            todayGoals = finalGoals,
            allGoals = actualAllList,
            selectedDate = args[5] as Long,
            selectedCategory = currentCategory,
            headerTitle = formatHeaderDate(args[5] as Long),
            dailyCompletionMap = completionMap,
            dailyValueMap = valueMap,
            notifications = notifications,
            unreadNotificationCount = unreadCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun markNotificationAsRead(id: Int) = viewModelScope.launch {
        notificationDao.markAsRead(id)
    }

    fun deleteNotification(id: Int) = viewModelScope.launch {
        notificationDao.deleteNotification(id)
    }

    fun markAllNotificationsAsRead() = viewModelScope.launch {
        notificationDao.markAllAsRead()
    }

    fun clearAllNotifications() = viewModelScope.launch {
        notificationDao.deleteAllNotifications()
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onDateSelected(timestamp: Long) {
        _selectedDate.value = getStartOfDay(timestamp)
    }

    fun incrementProgress(goal: SMARTGoalEntity, customValue: Double = 1.0) {
        // Gelecek günler için güncelleme yapılamaz
        val todayStart = getStartOfDay(System.currentTimeMillis())
        if (_selectedDate.value > todayStart) {
            return
        }
        
        viewModelScope.launch {
            val existingProgress = goalDao.getDailyProgressForDate(goal.id, _selectedDate.value)
            val currentDailyValue = existingProgress?.value ?: 0.0

            if (goal.measurementType == "Sayisal") {
                if (currentDailyValue >= goal.targetValue) {
                    return@launch
                }
            } else {
                if (currentDailyValue > 0) {
                    return@launch
                }
            }
            
            val newDailyValue = if (goal.measurementType == "Sayisal") {
                (currentDailyValue + customValue).coerceAtMost(goal.targetValue)
            } else {
                1.0
            }

            val nextTotalValue = (goal.currentValue + customValue).coerceAtMost(goal.targetValue)
            
            val isNowCompleted = nextTotalValue >= goal.targetValue

            // Hedefi güncelle
            goalDao.updateGoal(goal.copy(
                currentValue = nextTotalValue,
                isCompleted = isNowCompleted
            ))

            // Günlük progress'i kaydet
            goalDao.insertDailyProgress(
                DailyProgressEntity(
                    goalId = goal.id,
                    date = _selectedDate.value,
                    value = newDailyValue
                )
            )
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun formatHeaderDate(timestamp: Long): String {
        val today = getStartOfDay(System.currentTimeMillis())
        return when (timestamp) {
            today -> "Bugün"
            today + 86400000 -> "Yarın"
            today - 86400000 -> "Dün"
            else -> SimpleDateFormat("dd MMMM", Locale("tr")).format(Date(timestamp))
        }
    }

    private fun isGoalVisibleOnDate(goal: SMARTGoalEntity, dateTimestamp: Long): Boolean {
        val goalStart = getStartOfDay(goal.startDate)
        val goalEnd = getStartOfDay(goal.endDate)

        if (dateTimestamp < goalStart || dateTimestamp > goalEnd) {
            return false
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = dateTimestamp }

        // Kotlin Calendar'da Pazar=1, Pzt=2... Bizim UI'da Pzt=1. Bu yüzden dönüştürme yapıyoruz
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        return when (goal.frequency) {
            "Günlük", "Gunluk" -> true
            "Haftalık", "Haftalik" -> {
                val selectedDays = goal.selectedDays?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                selectedDays.contains(dayOfWeek)
            }
            "Aylık", "Aylik" -> {
                val selectedDays = goal.selectedDays?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                selectedDays.contains(dayOfMonth)
            }
            else -> true
        }

    }
}

















