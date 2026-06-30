package com.akhnaton.foodvisits.data.model.getItems

data class Data(
    val DESCRIPTION: String,
    val INVENTORY_ITEM_ID: String,
    val IS_BACK_ORDER: Boolean,
    val ITEM_CODE: String,
    val ITEM_NAME: String,
    val ITEM_TYPE: String,
    val ORDER_FLAG: String,
    val ORDER_STATUS: String,
    val ORIG_SYS_DOCUMENT_REF: String,
    val PRICE_WITHOUT_TAX: String,
    val QUANTITY: String,
    val RNUM: String,
    val TAX: String,
    val TOTAL_VALUE: String,
    val UNIT_PRICE: String
)