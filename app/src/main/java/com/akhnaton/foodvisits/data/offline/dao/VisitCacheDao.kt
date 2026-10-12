package com.akhnaton.foodvisits.data.offline.dao

import androidx.room.*
import com.akhnaton.foodvisits.data.offline.entity.VisitCacheEntity

@Dao
abstract class VisitCacheDao {

    @Query("SELECT * FROM visit_cache")
    abstract suspend fun getAll(): List<VisitCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(visits: List<VisitCacheEntity>)

    @Query("DELETE FROM visit_cache")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(visits: List<VisitCacheEntity>) {
        deleteAll()
        insertAll(visits)
    }
}