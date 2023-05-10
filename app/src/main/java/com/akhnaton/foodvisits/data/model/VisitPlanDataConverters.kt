package com.akhnaton.foodvisits.data.model

import androidx.room.TypeConverter
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisitData
import com.google.gson.Gson

class VisitPlanDataConverters {
    @TypeConverter
    fun stringToVisitPlanData(data: String?): VisitPlanData {
        return Gson().fromJson(data, VisitPlanData::class.java)
    }

    @TypeConverter
    fun visitPlanDataToString(objects: VisitPlanData): String {
        return Gson().toJson(objects)
    }

}