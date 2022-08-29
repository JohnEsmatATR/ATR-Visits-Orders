package com.akhnaton.foodvisits.data.model.order

data class Categories(
    var status: Int,
    var data: CategoriesData
)

data class CategoriesData(
    val sub_categories: Array<String>
)