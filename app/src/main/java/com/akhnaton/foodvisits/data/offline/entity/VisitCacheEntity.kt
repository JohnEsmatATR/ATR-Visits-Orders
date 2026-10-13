package com.akhnaton.foodvisits.data.offline.entity

import androidx.room.Entity
import com.akhnaton.foodvisits.data.model.visitPlan.VisitItem

@Entity(tableName = "visit_cache", primaryKeys = ["userId", "id"])
data class VisitCacheEntity(
    val userId: String,
    val id: String,
    val start: String,
    val customer_name: String,
    val customer_code: String,
    val site_address: String,
    val party_site: String,
    val sales_man: String,
    val supervisor: String,
    val approve: String?
)

fun VisitItem.toCacheEntity(userId: String) = VisitCacheEntity(
    userId, id, start, customer_name, customer_code, site_address,
    party_site, sales_man, supervisor, approve
)

fun VisitCacheEntity.toVisitItem() = VisitItem(
    id, start, customer_name, customer_code, site_address,
    party_site, sales_man, supervisor, approve
)