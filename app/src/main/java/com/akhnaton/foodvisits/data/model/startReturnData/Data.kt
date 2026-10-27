package com.akhnaton.foodvisits.data.model.startReturnData

import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists

data class Data(
    val products: List<Product>,
    val return_id: String,
    val select_list: List<SelectLists>
)