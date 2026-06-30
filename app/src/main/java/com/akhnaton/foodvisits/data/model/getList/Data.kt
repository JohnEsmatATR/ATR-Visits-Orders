package com.akhnaton.foodvisits.data.model.getList

data class Data(
    val CUSTOMER_CODE: String,
    val CUSTOMER_NAME: String,
    val ITEMS_COUNT: String,
    val ITEMS_PREVIEW: List<ITEMSPREVIEW>,
    val ORDER_DATE: String,
    val ORDER_FLAG: String,
    val ORDER_STATUS: String,
    val ORDER_TYPE: String,
    val ORIG_SYS_DOCUMENT_REF: String,
    val PARTY_SITE_ID: String,
    val RNUM: String,
    val TOTAL_QUANTITY: String,
    val TOTAL_VALUE: String
)