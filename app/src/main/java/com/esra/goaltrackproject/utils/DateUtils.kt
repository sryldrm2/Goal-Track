package com.esra.goaltrackproject.utils

import java.text.SimpleDateFormat
import java.time.Month
import java.util.Calendar
import java.util.Locale

fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
    return sdf.format(this)
}

fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
    return sdf.format(this)
}

fun createTimestamp(year: Int, month: Int, day: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun createTimeTimestamp(hour: Int, minute: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}