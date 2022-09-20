package com.akhnaton.foodvisits.shared

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

object ConvertDate {
    private var calendar: Date = Calendar.getInstance().time
    private var day = SimpleDateFormat("EEEE", Locale.ENGLISH)
    private var date = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    private var dateAndTime = SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a", Locale.ENGLISH)
    private var tsLong = System.currentTimeMillis() / 1000
    private var ts = tsLong.toString()

    fun getDay(): String {
        return day.format(calendar)
    }

    fun getDate(): String {
        return date.format(calendar)
    }

    fun getDateAndTime(): String {
        return dateAndTime.format(calendar)
    }

    fun getDateTimeStamp(): String {
        return ts
    }

    fun getDateTime(sDate: Long): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy")

//      val stamp = Timestamp(sDate)
        val date = Date(sDate * 1000)

        return sdf.format(date)
    }


}