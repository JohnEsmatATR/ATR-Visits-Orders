package com.akhnaton.foodvisits.data.model.promoter

import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent

data class CompetitorListModel(
    val status: Int,
    val data: CompetitorList,
)

data class CompetitorList(
    val get_competitor: List<GetCompetitor>,
    val get_competitor_types: List<GetCompetitorTypes>,
    val get_promotion_types: List<GetPromotionTypes>,
    val get_item_sizes: List<PromoterIntent.GetItemSizes>,
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