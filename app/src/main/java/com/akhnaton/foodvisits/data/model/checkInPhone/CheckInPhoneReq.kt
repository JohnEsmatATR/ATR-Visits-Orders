package com.akhnaton.foodvisits.data.model.checkInPhone

data class CheckInPhoneReq(
    val insert: Int,
    val ord_type: String,
    val party_site_id: String,
    val phone_visit: String
)