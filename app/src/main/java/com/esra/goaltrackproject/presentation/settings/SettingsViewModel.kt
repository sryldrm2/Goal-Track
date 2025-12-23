package com.esra.goaltrackproject.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.esra.goaltrackproject.R
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.UserPreferences

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val goalDao: GoalDao
) : ViewModel() {

    var userName by mutableStateOf(userPreferences.getUserName())
    var avatarResId by mutableIntStateOf(userPreferences.getAvatar(R.drawable.avatar1))
    var themeMode by mutableIntStateOf(userPreferences.getThemeMode())
    var notificationsEnabled by mutableStateOf(userPreferences.getNotificationsEnabled())

    fun updateUserName(newName: String) {
        userPreferences.saveUserName(newName)
        userName = newName
    }

    fun updateAvatar(newAvatarResId: Int) {
        userPreferences.saveAvatar(newAvatarResId)
        avatarResId = newAvatarResId
    }

    fun updateThemeMode(mode: Int) {
        userPreferences.saveThemeMode(mode)
        themeMode = mode
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        userPreferences.saveNotificationsEnabled(enabled)
        notificationsEnabled = enabled
    }
}