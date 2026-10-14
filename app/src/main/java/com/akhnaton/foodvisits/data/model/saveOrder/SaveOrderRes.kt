package com.akhnaton.foodvisits.data.model.saveOrder

import com.akhnaton.foodvisits.shared.gson.StringOrListAdapter
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter

data class SaveOrderRes(
    val `data`: JsonElement?,
    @JsonAdapter(StringOrListAdapter::class)
    val message: List<String>,
    val status: Int,
    val type: String
)