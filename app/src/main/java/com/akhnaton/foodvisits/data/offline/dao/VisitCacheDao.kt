package com.akhnaton.foodvisits.data.offline.dao

import androidx.room.*
import com.akhnaton.foodvisits.data.offline.entity.VisitCacheEntity

@Dao
abstract class VisitCacheDao {

    @Query("SELECT * FROM visit_cache WHERE userId = :userId")
    abstract suspend fun getAll(userId: String): List<VisitCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(visits: List<VisitCacheEntity>)

    @Query("DELETE FROM visit_cache WHERE userId = :userId")
    abstract suspend fun deleteAll(userId: String)

    @Transaction
    open suspend fun replaceAll(userId: String, visits: List<VisitCacheEntity>) {
        deleteAll(userId)
        insertAll(visits)
    }
}