package com.example.studeytrackerapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    fun formatDate(date: Date): String {
        val calendar = Calendar.getInstance()
        val today = calendar.clone() as Calendar
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val dateCalendar = Calendar.getInstance()
        dateCalendar.time = date
        dateCalendar.set(Calendar.HOUR_OF_DAY, 0)
        dateCalendar.set(Calendar.MINUTE, 0)
        dateCalendar.set(Calendar.SECOND, 0)
        dateCalendar.set(Calendar.MILLISECOND, 0)
        
        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_MONTH, -1)
        
        return when {
            dateCalendar == today -> "Today"
            dateCalendar == yesterday -> "Yesterday"
            else -> dateFormat.format(date)
        }
    }
    
    fun formatDateTime(date: Date): String {
        val calendar = Calendar.getInstance()
        val today = calendar.clone() as Calendar
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val dateCalendar = Calendar.getInstance()
        dateCalendar.time = date
        
        val dateCalendarOnly = dateCalendar.clone() as Calendar
        dateCalendarOnly.set(Calendar.HOUR_OF_DAY, 0)
        dateCalendarOnly.set(Calendar.MINUTE, 0)
        dateCalendarOnly.set(Calendar.SECOND, 0)
        dateCalendarOnly.set(Calendar.MILLISECOND, 0)
        
        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_MONTH, -1)
        
        val datePart = when {
            dateCalendarOnly == today -> "Today"
            dateCalendarOnly == yesterday -> "Yesterday"
            else -> dateFormat.format(date)
        }
        
        return "$datePart, ${timeFormat.format(date)}"
    }
    
    fun getWeekStartDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_WEEK, -calendar.get(Calendar.DAY_OF_WEEK) + 1)
        return calendar.time
    }
    
    fun getWeekEndDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
    
    fun getLast7DaysStart(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        return calendar.time
    }
    
    fun getLast7DaysEnd(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
}

