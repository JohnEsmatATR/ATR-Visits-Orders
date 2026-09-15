package com.akhnaton.foodvisits.data.model

data class Visit(
    val id: Int,
    val pharmacyName: String,
    val code: String,
    val locationCode: String,
    val repName: String,
    val address: String,
    val date: String,
    val isApproved: Boolean
)