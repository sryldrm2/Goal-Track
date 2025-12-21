package com.esra.goaltrackproject.presentation.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.SMARTGoalEntity
import com.esra.goaltrackproject.data.DailyProgressEntity
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
        "Sağlık" to Icons.Default.Favorite,
        "İş" to Icons.Default.Work,
        "Eğitim" to Icons.Default.School
    )
)

class DashboardViewModel(
    private val goalDao: GoalDao
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
        _selectedCategory
    ) { args: Array<Any?> ->
        val active = args[0] as Int
        val completed = args[1] as Int
        val nearest = args[2] as SMARTGoalEntity?
        val filteredByDate = args[3] as List<SMARTGoalEntity>
        val actualAllList = args[4] as List<SMARTGoalEntity>
        val selectedDate = args[5] as Long
        val currentCategory = args[6] as String

        val finalGoals = if (currentCategory == "Tümü") {
            filteredByDate
        } else {
            filteredByDate.filter { it.category.equals(currentCategory, ignoreCase = true) }
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
            // Her hedef için günlük progress yüzdesini hesapla ve ortalamasını al
            val dailyProgresses = finalGoals.map { goal ->
                val dailyValue = valueMap[goal.id] ?: 0.0
                if (goal.measurementType == "Sayisal") {
                    // Sayısal hedefler için: (günlük değer / hedef değer) * 100
                    ((dailyValue / goal.targetValue).coerceIn(0.0, 1.0) * 100).toInt()
                } else {
                    // Tamamlanma hedefleri için: Yapıldıysa 100, yapılmadıysa 0
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
            totalProgressPercent = dailyProgressPercent, // Seçilen güne özel günlük progress
            todayGoals = finalGoals,
            allGoals = actualAllList,
            selectedDate = args[5] as Long,
            selectedCategory = currentCategory,
            headerTitle = formatHeaderDate(args[5] as Long),
            dailyCompletionMap = completionMap,
            dailyValueMap = valueMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

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
            
            // Günlük limit kontrolü
            if (goal.measurementType == "Sayisal") {
                // Sayısal hedefler için günlük hedef kontrolü
                if (currentDailyValue >= goal.targetValue) {
                    // Günlük hedefe ulaşılmış, daha fazla artırma yapılamaz
                    return@launch
                }
            } else {
                // Tamamlanma hedefleri için: Zaten yapıldıysa tekrar yapma
                if (currentDailyValue > 0) {
                    return@launch
                }
            }
            
            val newDailyValue = if (goal.measurementType == "Sayisal") {
                // Sayısal: Günlük değeri artır
                (currentDailyValue + customValue).coerceAtMost(goal.targetValue)
            } else {
                // Tamamlanma: 1.0 olarak işaretle
                1.0
            }

            // Toplam currentValue'yu güncelle (sadece sayısal hedefler için)
            val nextTotalValue = if (goal.measurementType == "Sayisal") {
                (goal.currentValue + customValue).coerceAtMost(goal.targetValue)
            } else {
                goal.currentValue + 1.0
            }
            
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
}

















