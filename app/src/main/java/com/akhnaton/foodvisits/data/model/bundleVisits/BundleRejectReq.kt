package com.akhnaton.foodvisits.data.model.bundleVisits

data class BundleRejectReq(
    val is_coded: Int,
    val cust_code: String?,
    val takes_bundle: Int = 0,
    val reject_reason_id: Int,
    val latitude: Double,
    val longitude: Double,
)