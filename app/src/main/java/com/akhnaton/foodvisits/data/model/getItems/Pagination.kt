package com.akhnaton.foodvisits.data.model.getItems

data class Pagination(
    val current_page: Int,
    val page_size: Int,
    val total_pages: Int,
    val total_records: Int
)