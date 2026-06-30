package com.akhnaton.foodvisits.data.model.getStartOrderData

data class SelectLists(
    val required: Int,
    val select: String,
    val select_list: List<Select>,
    val select_name: String,
    var selectedValue: String? = null,
    var selectedId: String? = null
)

data class SelectedOption(
    val key: String,
    val id: String,
    val value: String
)