package com.akhnaton.foodvisits.data.model.bundleVisits

data class BundleRejectReq(
    val visit_id: String,
    val status: Int = 0,
    val reject_reason_id: Int,
    val latitude: Double,
    val longitude: Double,
)