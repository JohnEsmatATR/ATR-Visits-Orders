package com.akhnaton.foodvisits.data.db.dao

import androidx.room.*
import com.akhnaton.foodvisits.data.db.model.SaveVisitDB
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit

@Dao
interface SaveVisitDao {
    @Query("SELECT * FROM save_visit")
    suspend fun getVisits(): List<SaveVisitDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: SaveVisitDB)

    @Query("DELETE FROM save_visit")
    suspend fun deleteVisit()
}