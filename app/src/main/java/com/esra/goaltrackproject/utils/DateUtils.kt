package com.esra.goaltrackproject.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarDay(
    val dayName: String,
    val dayNumber: String,
    val dateInMillis: Long,
    val isToday: Boolean
)

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

