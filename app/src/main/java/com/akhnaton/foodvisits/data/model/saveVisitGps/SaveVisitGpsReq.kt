package com.akhnaton.foodvisits.data.model.saveVisitGps

data class SaveVisitGpsReq(
    val act_target: Int,
    val another_order_type: String,
    val check_in: String,
    val comment: String,
    val device_type: String,
    val grade: String,
    val ord_type: String,
    val party_site_id: String,
    val phone_visit: String,
    val visibility: String,
    val visit_target: Int,
    val latitude: String,
    val longitude: String,
    val rate: String,
    val rate_comment: String,
    val visit_with_confirmed: String,
    val visit_with_user_id: String? = null,
    val zone_flag: String
)