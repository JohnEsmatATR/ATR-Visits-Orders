package com.akhnaton.foodvisits.data.offline.entity

import androidx.room.Entity

@Entity(
    tableName = "pending_save_visit",
    primaryKeys = ["userId", "partySiteId", "ordType", "visitDate"]
)
data class PendingSaveVisitEntity(
    val userId: String,
    val partySiteId: String,
    val ordType: String,
    val visitDate: String,
    val requestJson: String,
    val createdAt: Long,
    val status: String = "pending"
)