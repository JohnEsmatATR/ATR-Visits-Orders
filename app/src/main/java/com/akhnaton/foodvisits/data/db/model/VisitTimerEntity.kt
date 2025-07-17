package com.akhnaton.foodvisits.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visit_timer")
data class VisitTimerEntity(
    @PrimaryKey val customerPartySiteId: String,
    val startTimeMillis: Long,
    val startLat: String,
    val startLong : String
)
