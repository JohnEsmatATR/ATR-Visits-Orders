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
    val date_of_visit: String,
    val valid_gps_range: Int,
    val visit_detail_id: String,
    val visit_with_user_id: String,
    val visit_with_name: String,
    val is_visited_today: Boolean
)