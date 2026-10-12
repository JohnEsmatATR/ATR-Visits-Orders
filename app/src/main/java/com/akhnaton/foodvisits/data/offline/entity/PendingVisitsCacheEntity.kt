package com.akhnaton.foodvisits.data.offline.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_visits_cache")
data class PendingVisitsCacheEntity(
    @PrimaryKey val cacheKey: String,
    val json: String
)