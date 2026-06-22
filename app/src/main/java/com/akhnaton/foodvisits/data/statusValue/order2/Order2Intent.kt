package com.akhnaton.foodvisits.data.statusValue.order2

import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.google.gson.JsonElement

sealed class Order2Intent {

    data class GetStartOrderData(
        val partySiteId: String,
        val orderType: String,
        val customerCode: String,
    ) : Order2Intent()

    data class RefreshToken(val userId: String, val token: String) : Order2Intent()

}