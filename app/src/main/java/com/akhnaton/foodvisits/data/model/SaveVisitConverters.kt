package com.akhnaton.foodvisits.data.model

import androidx.room.TypeConverter
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisitData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class SaveVisitConverters {
    @TypeConverter
    fun stringToSaveVisit(data: String?): SaveVisitData {
        return Gson().fromJson(data, SaveVisitData::class.java)
    }

    @TypeConverter
    fun saveVisitToString(objects: SaveVisitData): String {
        return Gson().toJson(objects)
    }
}