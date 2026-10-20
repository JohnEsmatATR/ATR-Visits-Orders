package com.akhnaton.foodvisits.data.offline.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhnaton.foodvisits.data.offline.entity.LocalVisitStartEntity

@Dao
interface LocalVisitStartDao {

    @Query(
        "SELECT * FROM local_visit_start WHERE userId = :userId AND customerCode = :customerCode " +
                "AND partySiteId = :partySiteId AND visitDate = :visitDate LIMIT 1"
    )
    suspend fun get(
        userId: String, customerCode: String, partySiteId: String, visitDate: String
    ): LocalVisitStartEntity?
    @Query("DELETE FROM local_visit_start WHERE userId = :userId AND customerCode = :customerCode AND partySiteId = :partySiteId AND visitDate = :visitDate")
    suspend fun delete(userId: String, customerCode: String, partySiteId: String, visitDate: String)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: LocalVisitStartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LocalVisitStartEntity)
}