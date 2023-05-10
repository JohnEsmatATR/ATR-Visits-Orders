package com.akhnaton.foodvisits.data.model

import androidx.room.TypeConverter
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisitData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ListVisitPlanConverters {
    @TypeConverter
    fun stringToListPlan(data: String?): List<CustomerVisitPlan> {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object :
            TypeToken<List<CustomerVisitPlan>>() {}.type
        return Gson().fromJson<List<CustomerVisitPlan>>(data, listType)
    }

    @TypeConverter
    fun listPlanToString(someObjects: List<CustomerVisitPlan>): String? {
        return Gson().toJson(someObjects)
    }
}