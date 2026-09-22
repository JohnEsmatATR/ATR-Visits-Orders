package com.akhnaton.foodvisits.data.model.visitPlan

import com.google.gson.JsonElement

data class CustomerItem(
    val CUSTOMER_NAME: String,
    val SITE_ADDRESS: String,
    val CUSTOMER_CODE: String,
    val TEAM_NAME: String,
    val CUSTOMER_PROFILE_CLASS: String,
    val PARTY_SITE_ID: String,
    val CUSTOMER_BRANCH: String,
    val AREA: String?,
    val DEFINTION: String
)

data class GetVisitCustomersData(
    val setup_customers: List<CustomerItem>
)

data class GetVisitCustomersRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)