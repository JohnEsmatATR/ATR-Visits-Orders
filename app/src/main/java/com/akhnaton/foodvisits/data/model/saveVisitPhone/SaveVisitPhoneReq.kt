package com.akhnaton.foodvisits.data.model.saveVisitPhone

data class SaveVisitPhoneReq(
    val act_target: Int? = null,
    val another_order_type: String? = null,
    val check_in: String,
    val comment: String? = null,
    val device_type: String,
    val grade: String? = null,
    val ord_type: String,
    val party_site_id: String,
    val phone_visit: String? = null,
    val visibility: String? = null,
    val visit_target: Int? = null
)