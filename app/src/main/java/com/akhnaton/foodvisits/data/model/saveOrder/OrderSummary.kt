package com.akhnaton.foodvisits.data.model.saveOrder

data class OrderSummary(
    val order_type: String,
    val party_site: PartySite,
    val selects: List<Select>,
    val visit_id: String
)