package com.esra.goaltrackproject.presentation.dashboard

import androidx.lifecycle.ViewModel
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.SMARTGoalEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class DashboardUiState(
    val activeGoalsCount: Int = 0,
    val completedGoalsCount: Int = 0,
    val nearestGoal: SMARTGoalEntity? = null,
    val totalProgressPercent: Int = 0,
    val todayGoals: List<SMARTGoalEntity> = emptyList(),
    val allGoals: List<SMARTGoalEntity> = emptyList()
)

class DashboardViewModel(
    private val goalDao: GoalDao
) : ViewModel() {

    private fun getTodayTimestampRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    private val timeRange = getTodayTimestampRange()

    val uiState: Flow<DashboardUiState> = combine(
        goalDao.countActiveGoals(),
        goalDao.countCompletedGoals(),
        goalDao.getNearestDeadlineGoal(),
        goalDao.getTodayGoals(timeRange.first, timeRange.second),
        goalDao.getAllGoals()
    ) { active, completed, nearest, todayList, allList ->

        val totalGoals = allList.size
        val totalProgress = if (totalGoals > 0) {
            allList.sumOf { it.progressPercent } / totalGoals
        } else {
            0
        }

        DashboardUiState(
            activeGoalsCount = active,
            completedGoalsCount = completed,
            nearestGoal = nearest,
            totalProgressPercent = totalProgress,
            todayGoals = todayList,
            allGoals = allList
        )
    }
}
