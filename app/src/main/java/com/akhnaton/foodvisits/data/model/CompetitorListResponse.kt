package com.akhnaton.foodvisits.data.model

data class CompetitorListResponse(
    val status: Int,
    val message: String,
    val type: String,
    val data: CompetitorListData
)

data class CompetitorListData(
    val get_competitor: List<CompetitorItem>,
    val get_competitor_types: List<CompetitorTypeItem>,
    val get_promotion_types: List<PromotionTypeItem>
)

data class CompetitorItem(
    val id: String,
    val competitor_name: String
)

data class CompetitorTypeItem(
    val id: String,
    val type_name: String
)

data class PromotionTypeItem(
    val id: String,
    val name: String
)