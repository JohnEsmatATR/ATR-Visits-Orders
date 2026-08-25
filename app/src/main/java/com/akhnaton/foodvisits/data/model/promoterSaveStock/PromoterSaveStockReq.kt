package com.akhnaton.foodvisits.data.model.promoterSaveStock

data class PromoterSaveStockReq(
    val customer_code: String,
    val items: List<Item>,
    val party_site_id: String
)