package com.akhnaton.foodvisits.data.model.saveVisitPhone

import com.google.gson.JsonElement

data class SaveVisitPhoneRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)