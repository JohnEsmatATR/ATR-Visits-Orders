package com.akhnaton.foodvisits.data.model.startReturnData

data class Product(
    val CATEGORY_ID: String,
    val DESCRIPTION: String,
    val INVENTORY_ITEM_ID: String,
    val IS_SELECTED: Boolean,
    val ITEM_CODE: String,
    val SAVED_ITEMS: List<SAVEDITEMS>,
    val SEGMENT2: String,
    val SEGMENT4: String
)