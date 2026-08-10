package com.akhnaton.foodvisits.shared

import java.text.SimpleDateFormat
import java.util.Locale

fun convertDateToApiFormat(date: String): String {
    val inputFormat = SimpleDateFormat(
        "dd MMMM yyyy",
        Locale.ENGLISH
    )

    val outputFormat = SimpleDateFormat(
        "dd-MM-yyyy",
        Locale.ENGLISH
    )

    return outputFormat.format(inputFormat.parse(date)!!)
}