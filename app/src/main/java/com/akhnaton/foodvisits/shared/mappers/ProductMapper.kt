package com.akhnaton.foodvisits.shared.mappers

import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.saveOrder.ItemsSummary

object ProductMapper {

    fun mapItemSummaryToProducts(
        itemSummaries: List<ItemsSummary>,
        products: List<Product>
    ): List<Product> {

        val productsMap = products.associateBy { it.INVENTORY_ITEM_ID }

        return itemSummaries.mapNotNull { summary ->

            val product = productsMap[summary.PRODUCT_ID] ?: return@mapNotNull null

            product.copy(
                selectedQty = summary.REQUESTED_QUANTITY,
                TOTAL_QUANTITY = summary.AVAILABLE_QUANTITY,
                IS_BACK_ORDER = summary.IS_BACK_ORDER,
                MESSAGE = summary.MESSAGE,
                CHECKED = true,
            )
        }
    }
}