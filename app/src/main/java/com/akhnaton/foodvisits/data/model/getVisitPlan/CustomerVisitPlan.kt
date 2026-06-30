package com.akhnaton.foodvisits.data.model.getVisitPlan

data class CustomerVisitPlan(
    val customer_address: String,
    val customer_code: String,
    val customer_latitude: Double,
    val customer_line_id: String,
    val customer_longitude: Double,
    val customer_name: String,
    val customer_order_type: String,
    val customer_party_site_id: String,
    val customer_type: String,
    val valid_gps_range: Int,
    val is_visited_today: Boolean
)