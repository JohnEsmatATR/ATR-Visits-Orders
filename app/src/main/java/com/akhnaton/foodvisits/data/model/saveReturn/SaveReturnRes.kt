package com.akhnaton.foodvisits.data.model.saveReturn

import com.google.gson.JsonElement

data class SaveReturnRes(
    val `data`: JsonElement?,
    val message: JsonElement?,
    val status: Int,
    val type: String
)
fun JsonElement?.asMessage(): String = when {
    this == null || isJsonNull -> ""
    isJsonArray -> asJsonArray.firstOrNull()?.asString.orEmpty()
    else -> asString
}