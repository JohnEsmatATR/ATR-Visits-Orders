package com.akhnaton.foodvisits.shared

import java.util.Locale

object NumberUtils {
    fun money(value: Double): String = String.format(Locale.US, "%,.2f", value)
}