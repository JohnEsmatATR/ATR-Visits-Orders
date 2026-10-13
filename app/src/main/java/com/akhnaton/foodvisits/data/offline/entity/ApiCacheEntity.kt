package com.akhnaton.foodvisits.data.offline.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_cache")
data class ApiCacheEntity(
    @PrimaryKey val cacheKey: String,
    val data: String,
    val message: String,
    val type: String
)