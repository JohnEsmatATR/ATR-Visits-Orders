package com.akhnaton.foodvisits.data.model

data class GovernoratesResponse(
    val status: Int,
    val data: GovernoratesData
)

data class GovernoratesData(
    val governorateS: List<Governorate>
)

data class Governorate(
    val id: String,
    val name_ar: String,
    val name_en: String
)
