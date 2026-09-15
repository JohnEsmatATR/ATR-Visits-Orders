package com.akhnaton.foodvisits.data.dummy

import com.akhnaton.foodvisits.data.model.Visit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DummyVisitsProvider {

    fun generate(): List<Visit> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2026)
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val visits = mutableListOf<Visit>()
        var idCounter = 1

        val samplePharmacies = listOf(
            "صيدلية مجدى حبيب" to "المندوب: مارى ماهر اديب خليل",
            "صيدلية النور" to "المندوب: أحمد سمير فتحي",
            "صيدلية ماركو نجيب" to "المندوب: مارى ماهر اديب خليل",
            "صيدلية الرحمة" to "المندوب: كريم عادل صابر",
            "صيدلية الشفاء" to "المندوب: نهال محمد عبده"
        )

        val sampleAddresses = listOf(
            "شارع وابور الترجمان بولاق مصر",
            "المسافر خانة القلل الازبكية القاهرة",
            "شارع الجمهورية المنصورة",
            "شارع الهرم الجيزة",
            "كورنيش النيل المعادي"
        )

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = sdf.format(cal.time)

            val visitsCountForDay = if (day % 7 == 0) 0 else (1..2).random()

            repeat(visitsCountForDay) {
                val (pharmacy, rep) = samplePharmacies.random()
                visits.add(
                    Visit(
                        id = idCounter++,
                        pharmacyName = pharmacy,
                        code = (10000..10999).random().toString(),
                        locationCode = (3698000..3699000).random().toString(),
                        repName = rep.removePrefix("المندوب: "),
                        address = sampleAddresses.random(),
                        date = dateKey,
                        isApproved = listOf(true, false).random()
                    )
                )
            }
        }

        return visits
    }
}