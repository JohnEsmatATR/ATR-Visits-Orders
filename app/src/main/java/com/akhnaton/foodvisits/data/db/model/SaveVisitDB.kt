package com.akhnaton.foodvisits.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_visit")
data class SaveVisitDB(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val version: String,
    val token: String,
    val customerPartySiteId: String,
    val visitType: String,
    val visitarget: String,
    val visitActualTarget: String,
    val latitude: String,
    val longitude: String,
    val deviceType: String,
    val zoneFlag: String,
    val checkInDate: String,
    val dateVisit: String,
    val customerType: String,
    val orderType: String,
)