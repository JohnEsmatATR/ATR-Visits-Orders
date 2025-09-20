package com.akhnaton.foodvisits.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhnaton.foodvisits.data.db.model.VisitTimerEntity

@Dao
interface VisitTimerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitTimer(timer: VisitTimerEntity)

    @Query("SELECT * FROM visit_timer WHERE customerPartySiteId = :id LIMIT 1")
    suspend fun getVisitTimerById(id: String): VisitTimerEntity?

    @Query("DELETE FROM visit_timer WHERE customerPartySiteId = :id")
    suspend fun deleteVisitTimerById(id: String)

    @Query("SELECT * FROM visit_timer")
    suspend fun getAllVisitTimers(): List<VisitTimerEntity>


}
