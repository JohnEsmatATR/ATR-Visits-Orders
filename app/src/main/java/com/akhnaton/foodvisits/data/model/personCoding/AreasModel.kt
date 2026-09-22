package com.akhnaton.foodvisits.data.model.personCoding

import com.google.gson.JsonElement

data class AreasModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)

data class AreasData(
    val areas: List<Area>?
)

data class Area(
    val name_ar: String,
    val name_en: String,
    val id: String
) {
    override fun toString(): String = name_ar
}