package com.akhnaton.foodvisits.data.model.nationalIdScan

data class NationalIdScanRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)