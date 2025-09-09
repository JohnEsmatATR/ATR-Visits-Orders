package com.akhnaton.foodvisits.data.model.createNewCustomer

data class AreasResponse(
    val status: Int,
    val data: AreasData
)

data class AreasData(
    val areas: List<Area>
)

data class Area(
    val id: String,
    val name_ar: String,
    val name_en: String
)

