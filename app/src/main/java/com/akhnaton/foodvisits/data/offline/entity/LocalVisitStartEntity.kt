package com.akhnaton.foodvisits.data.offline.entity

import androidx.room.Entity

@Entity(
    tableName = "local_visit_start",
    primaryKeys = ["userId", "customerCode", "partySiteId", "visitDate"]
)
data class LocalVisitStartEntity(
    val userId: String,
    val customerCode: String,
    val partySiteId: String,
    val visitDate: String,   // yyyy-MM-dd
    val startMillis: Long
)