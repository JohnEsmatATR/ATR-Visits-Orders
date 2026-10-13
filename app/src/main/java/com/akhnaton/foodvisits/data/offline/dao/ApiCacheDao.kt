package com.akhnaton.foodvisits.data.offline.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhnaton.foodvisits.data.offline.entity.ApiCacheEntity

@Dao
interface ApiCacheDao {

    @Query("SELECT * FROM api_cache WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun get(cacheKey: String): ApiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ApiCacheEntity)
}