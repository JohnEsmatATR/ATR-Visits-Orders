package com.akhnaton.foodvisits.data.model.nationalIdScan

import com.google.gson.JsonElement

data class NationalIdScanRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)