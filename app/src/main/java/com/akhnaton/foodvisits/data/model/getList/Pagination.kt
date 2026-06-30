package com.akhnaton.foodvisits.data.model.getList

data class Pagination(
    val current_page: Int,
    val page_size: Int,
    val total_pages: Int,
    val total_records: Int
)