package com.akhnaton.foodvisits.data.db.dao

import androidx.room.*
import com.akhnaton.foodvisits.data.model.VisitsPlan

@Dao
interface VisitPlanDao {
    @Query("SELECT * FROM visits_plan")
    suspend fun getPlan(): VisitsPlan

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: VisitsPlan)

    @Query("DELETE FROM visits_plan")
    suspend fun deletePlan()
}