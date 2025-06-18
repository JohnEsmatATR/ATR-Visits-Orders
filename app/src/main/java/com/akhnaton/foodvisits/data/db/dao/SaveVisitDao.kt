package com.akhnaton.foodvisits.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhnaton.foodvisits.data.db.model.SaveVisitDB

@Dao
interface SaveVisitDao {
    @Query("SELECT * FROM save_visit")
    suspend fun getVisits(): List<SaveVisitDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: SaveVisitDB): Long

    @Query("DELETE FROM save_visit")
    suspend fun deleteVisit(): Int
}
