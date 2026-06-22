package com.akhnaton.foodvisits.data.model.getCustomerData

data class CustomerAddres(
    val CATEGORY_CODE: String,
    val CUSTOMER_CODE: String,
    val CUSTOMER_ID: String,
    val CUSTOMER_NAME: String,
    val CUST_CONDATION: String,
    val MONTHLY_OPEN: String,
    val PARTY_SITE_ID: String,
    val PROVINCE: String,
    val SITE_ADDRESS: String,
    val TEAM_NAME: String,
    val TEL: List<Any>
)