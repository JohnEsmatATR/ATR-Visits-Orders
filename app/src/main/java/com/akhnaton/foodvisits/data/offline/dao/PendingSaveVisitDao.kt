package com.akhnaton.foodvisits.data.offline.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhnaton.foodvisits.data.offline.entity.PendingSaveVisitEntity

@Dao
interface PendingSaveVisitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingSaveVisitEntity)

    @Query("SELECT * FROM pending_save_visit WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getAll(userId: String): List<PendingSaveVisitEntity>

    @Query(
        "DELETE FROM pending_save_visit WHERE userId = :userId AND partySiteId = :partySiteId " +
                "AND ordType = :ordType AND visitDate = :visitDate"
    )
    suspend fun delete(userId: String, partySiteId: String, ordType: String, visitDate: String)
}