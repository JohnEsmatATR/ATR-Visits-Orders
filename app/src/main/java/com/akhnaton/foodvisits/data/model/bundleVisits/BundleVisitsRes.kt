package com.akhnaton.foodvisits.data.model.bundleVisits

import com.google.gson.JsonElement

data class BundleVisitsRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)