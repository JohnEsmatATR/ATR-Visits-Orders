package com.akhnaton.foodvisits.data.model.editOrder

import com.akhnaton.foodvisits.data.model.saveOrder.Item
import com.akhnaton.foodvisits.data.model.saveOrder.ItemsSummary

data class Data(
    val items: List<Item>,
    val items_summary: List<ItemsSummary>,
    val msg: String,
    val order_numbers: List<String>
)