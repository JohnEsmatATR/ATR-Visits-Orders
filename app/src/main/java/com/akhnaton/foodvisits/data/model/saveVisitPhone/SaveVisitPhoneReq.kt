package com.akhnaton.foodvisits.data.model.saveVisitPhone

data class SaveVisitPhoneReq(
    val check_in_date: Long,
    val customer_party_site_id: String,
    val customer_type: String,
    val date_visit: Long,
    val grade: String,
    val order_type: String,
    val phone_visit: String,
    val promoters_notes: String,
    val visit_actual_target: String,
    val visit_notes: String,
    val visit_target: String,
    val visit_visibility: String
)