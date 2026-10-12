package com.akhnaton.foodvisits.data.offline.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhnaton.foodvisits.data.offline.entity.PendingVisitsCacheEntity

@Dao
interface PendingVisitsCacheDao {

    @Query("SELECT json FROM pending_visits_cache WHERE cacheKey = :cacheKey")
    suspend fun get(cacheKey: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: PendingVisitsCacheEntity)
}