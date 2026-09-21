package com.akhnaton.foodvisits.data.model.personCoding

import com.akhnaton.foodvisits.shared.gson.StringOrListAdapter
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

data class AddCustomerModel(
    val status: Int,
    @JsonAdapter(StringOrListAdapter::class)
    val message: List<String>,
    val type: String,
    val data: Any? = null
)