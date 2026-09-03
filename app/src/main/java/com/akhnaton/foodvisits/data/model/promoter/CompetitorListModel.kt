package com.akhnaton.foodvisits.data.model.promoter

data class CompetitorListModel(
    val status: Int,
    val data: CompetitorList,
)

data class CompetitorList(
    val get_competitor: List<GetCompetitor>,
    val get_competitor_types: List<GetCompetitorTypes>,
    val get_promotion_types: List<GetPromotionTypes>,
)

data class GetCompetitor(
    val id: String,
    val competitor_name: String,
)

data class GetCompetitorTypes(
    val id: String,
    val type_name: String,
)

data class GetPromotionTypes(
    val id: String,
    val name: String,
)