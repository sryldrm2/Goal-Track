package com.esra.goaltrackproject.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("goaltrack_prefs", Context.MODE_PRIVATE)

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String {
        return sharedPreferences.getString("user_name", "Kullanıcı") ?: "Kullanıcı"
    }

    fun saveAvatar(drawableId: Int) {
        sharedPreferences.edit().putInt("user_avatar", drawableId).apply()
    }

    fun getAvatar(defaultResId: Int): Int {
        return sharedPreferences.getInt("user_avatar", defaultResId)
    }

    fun setOnboardingCompleted() {
        sharedPreferences.edit().putBoolean("onboarding_complete", true).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean("onboarding_complete", false)
    }
}