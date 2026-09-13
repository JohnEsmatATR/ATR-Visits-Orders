package com.akhnaton.foodvisits.data.model.personCoding

data class GovernoratesModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: GovernoratesData?
)

data class GovernoratesData(
    val governorateS: List<Governorate>?
)

data class Governorate(
    val name_ar: String,
    val name_en: String,
    val id: String
) {
    override fun toString(): String = name_ar
}