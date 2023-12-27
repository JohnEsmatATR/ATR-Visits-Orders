package com.akhnaton.foodvisits.data.model.promoter

data class CompetitorListModel(
    val status: Int,
    val data: CompetitorList,
)

data class CompetitorList(
    val get_competitor: List<GetCompetitor>,
)


data class GetCompetitor(
    val id: String,
    val competitor_name: String,
)
