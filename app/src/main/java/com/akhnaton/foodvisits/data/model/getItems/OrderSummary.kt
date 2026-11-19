package com.akhnaton.foodvisits.data.model.getItems

data class OrderSummary(
    val order_type: String?,
    val visit_id: String?,
    val party_site: PartySite?,
    val selects: List<OrderSelect>?
)

data class PartySite(
    val id: String?,
    val name: String?
)

data class OrderSelect(
    val key: String?,
    val value: String?,
    val name: String?
)