package com.akhnaton.foodvisits.data.model.getList

data class GetListRes(
    val `data`: List<Data>,
    val message: String,
    val pagination: Pagination,
    val params: Params,
    val status: Int,
    val type: String
)