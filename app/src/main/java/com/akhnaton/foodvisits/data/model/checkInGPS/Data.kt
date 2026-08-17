package com.akhnaton.foodvisits.data.model.checkInGPS

data class Data(
    val already_started: Boolean,
    val check_in: String?,
    val current_time: String,
    val message: String,
    val visit_id: Int?
)