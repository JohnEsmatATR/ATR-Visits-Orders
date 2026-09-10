package com.akhnaton.foodvisits.data.model.saveVisitGps

data class SaveVisitGpsReq(
    val act_target: Int? = null,
    val another_order_type: String? = null,
    val check_in: String? = null,
    val comment: String? = null,
    val device_type: String? = null,
    val grade: String? = null,
    val ord_type: String,
    val party_site_id: String,
    val phone_visit: String,
    val visibility: String? = null,
    val visit_target: Int? = null,
    val latitude: String,
    val longitude: String,
    val rate: String? = null,
    val rate_comment: String? = null,
    val visit_with_confirmed: String? = null,
    val visit_with_user_id: String? = null,
//    val zone_flag: String
    val check_zone_flag: String
)