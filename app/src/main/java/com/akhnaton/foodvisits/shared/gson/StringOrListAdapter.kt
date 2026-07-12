package com.akhnaton.foodvisits.shared.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class StringOrListAdapter : JsonDeserializer<List<String>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<String> {

        return when {
            json.isJsonArray ->
                json.asJsonArray.map { it.asString }

            json.isJsonPrimitive ->
                listOf(json.asString)

            else ->
                emptyList()
        }
    }
}