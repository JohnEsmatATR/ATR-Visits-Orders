package com.akhnaton.foodvisits.ui.home.order

import com.akhnaton.foodvisits.data.model.getStartOrderData.Product

object OrderStateHolder {
    var orderId: String? = null
    var customerPartySiteId: String? = null
    var customerCode: String? = null
    var saleType: String? = null
    var storeId: String? = null
    var priceListId: String? = null
    var visitId: String = ""
    var isSavedBefore: Boolean = false
    var selectedProducts: MutableList<Product> = mutableListOf()
    var orderSelects: MutableMap<String, String> = mutableMapOf()

    fun clear() {
        orderId = null
        customerPartySiteId = null
        customerCode = null
        saleType = null
        storeId = null
        priceListId = null
        visitId = ""
        isSavedBefore = false
        selectedProducts = mutableListOf()
        orderSelects = mutableMapOf()
    }
}