package com.akhnaton.foodvisits.shared

import com.google.gson.JsonElement

fun JsonElement?.getMessage(): String {

    if (this == null || this.isJsonNull)
        return ""

    return when {
        isJsonPrimitive -> asString

        isJsonArray ->
            if (asJsonArray.size() > 0)
                asJsonArray[0].asString
            else
                ""

        else -> toString()
    }
}