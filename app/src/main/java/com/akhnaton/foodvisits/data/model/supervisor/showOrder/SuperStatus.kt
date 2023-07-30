package com.akhnaton.foodvisits.data.model.supervisor.showOrder


data class SuperStatus(
    var message: String,
    var status: String,
    var data: List<SuperOrderStatus>
)