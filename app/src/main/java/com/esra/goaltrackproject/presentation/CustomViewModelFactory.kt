package com.esra.goaltrackproject.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.NotificationDao
import com.esra.goaltrackproject.presentation.addgoal.AddGoalViewModel
import com.esra.goaltrackproject.presentation.dashboard.DashboardViewModel
import com.esra.goaltrackproject.presentation.detail.GoalDetailViewModel

class CustomViewModelFactory(
    private val application: Application,
    private val goalDao: GoalDao,
    private val notificationDao: NotificationDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(goalDao, notificationDao) as T
            }
            modelClass.isAssignableFrom(AddGoalViewModel::class.java) -> {
                AddGoalViewModel(application, goalDao) as T
            }
            modelClass.isAssignableFrom(GoalDetailViewModel::class.java) -> {
                GoalDetailViewModel(application, goalDao) as T
            }
            else -> throw IllegalArgumentException("Bilinmeyen ViewModel Sınıfı: ${modelClass.name}")
        }
    }
}