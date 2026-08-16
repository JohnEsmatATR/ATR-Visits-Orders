package com.akhnaton.foodvisits.data.model.checkInGPS

data class CheckInGPSReq(
    val insert: Int,
    val latitude: String,
    val longitude: String,
    val ord_type: String,
    val party_site_id: String,
    val phone_visit: String
)