package com.akhnaton.foodvisits.utils

import java.math.BigDecimal

object NumberFormatter {

    @JvmStatic
    fun format(value: Double): String {
        val plain = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
        val parts = plain.split(".")
        val integerPart = groupThousands(parts[0])
        return if (parts.size > 1) "$integerPart.${parts[1]}" else integerPart
    }

    @JvmStatic
    fun format(value: Int): String {
        return groupThousands(value.toString())
    }

    private fun groupThousands(numberString: String): String {
        val isNegative = numberString.startsWith("-")
        val digitsOnly = if (isNegative) numberString.substring(1) else numberString
        val grouped = digitsOnly.reversed().chunked(3).joinToString(",").reversed()
        return if (isNegative) "-$grouped" else grouped
    }
}