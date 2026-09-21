package com.akhnaton.foodvisits.shared.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class StringOrListAdapter : TypeAdapter<List<String>>() {

    override fun read(reader: JsonReader): List<String> {
        return when (reader.peek()) {

            JsonToken.STRING -> {
                listOf(reader.nextString())
            }

            JsonToken.BEGIN_ARRAY -> {
                val result = mutableListOf<String>()

                reader.beginArray()

                while (reader.hasNext()) {
                    if (reader.peek() == JsonToken.STRING) {
                        result.add(reader.nextString())
                    } else {
                        reader.skipValue()
                    }
                }

                reader.endArray()

                result
            }

            JsonToken.NULL -> {
                reader.nextNull()
                emptyList()
            }

            else -> {
                reader.skipValue()
                emptyList()
            }
        }
    }

    override fun write(
        out: JsonWriter,
        value: List<String>?
    ) {
        if (value == null) {
            out.nullValue()
            return
        }

        out.beginArray()

        value.forEach {
            out.value(it)
        }

        out.endArray()
    }
}