package com.akhnaton.foodvisits.data.model.order

import java.io.Serializable

data class Product(
    var status: Int,
    var data: CollectData
)

data class CollectData(
    var products_bonus: List<BonusData>,
    var order_products: List<ProductData>,
    var return_products: List<ProductData>
)

data class BonusData(
    var item_name: String,
    var item_description: String
)

@kotlinx.serialization.Serializable
class ProductData(
    var item_id: Int,
    var item_code: Int,
    var item_description: String,
    var item_availability: Int,
    var item_price_list: Int,
    var item_price: Double,
    var item_tax: Double
) : Serializable

