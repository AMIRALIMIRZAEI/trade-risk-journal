package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

  private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
  private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  private val fullDateTimeFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
  private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

  fun formatDate(timestamp: Long): String {
    return dateFormat.format(Date(timestamp))
  }

  fun formatTime(timestamp: Long): String {
    return timeFormat.format(Date(timestamp))
  }

  fun formatDateTime(timestamp: Long): String {
    return fullDateTimeFormat.format(Date(timestamp))
  }

  fun formatMonthYear(calendar: Calendar): String {
    return monthYearFormat.format(calendar.time)
  }

  fun getDayKey(timestamp: Long): String {
    return dayKeyFormat.format(Date(timestamp))
  }

  fun getDayKey(calendar: Calendar): String {
    return dayKeyFormat.format(calendar.time)
  }

  fun getStartOfDay(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply {
      timeInMillis = timestamp
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
  }

  fun formatCurrency(amount: Double, prefix: String = "$"): String {
    val sign = if (amount < 0) "-" else ""
    val absAmount = kotlin.math.abs(amount)
    return String.format(Locale.US, "%s%s%,.2f", sign, prefix, absAmount)
  }

  fun formatPercent(percent: Double): String {
    val sign = if (percent > 0) "+" else ""
    return String.format(Locale.US, "%s%.2f%%", sign, percent)
  }

  fun formatNumber(number: Double, decimals: Int = 2): String {
    return String.format(Locale.US, "%,.${decimals}f", number)
  }
}
