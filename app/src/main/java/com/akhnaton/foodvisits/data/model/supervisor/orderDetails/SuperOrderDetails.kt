package com.akhnaton.foodvisits.data.model.supervisor.orderDetails

data class SuperOrderDetails(
    val item_description: String,
    val order_quantity: String,
    val price: String,
    val tax: String,
    val total_items: String,
    val return_flag: String,
    val item_qouta_status: String,
    val quota_quantity: String,
    val approve_stat_quota: String,
    val inventory_item_id: String,
    val quota_total: String,
    val checkapprove: String,
    val checkapprove_num: Int
)