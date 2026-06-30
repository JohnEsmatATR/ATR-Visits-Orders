package com.akhnaton.foodvisits.shared

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getTodayDayName(): String {
        return SimpleDateFormat(
            "EEEE",
            Locale.getDefault()
        ).format(Date())
    }

    fun getTodayDate(): String {
        return SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.ENGLISH
        ).format(Date())
    }
}