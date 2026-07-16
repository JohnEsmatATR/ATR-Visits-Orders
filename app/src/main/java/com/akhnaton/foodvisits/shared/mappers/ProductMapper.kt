package com.akhnaton.foodvisits.shared.mappers

import android.util.Log
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.saveOrder.ItemsSummary
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderItemReq

object ProductMapper {

    fun mapItemSummaryToProducts(
        itemSummaries: List<ItemsSummary>,
        products: List<Product>
    ): List<Product> {

        val productsMap = products.associateBy { it.INVENTORY_ITEM_ID }

        return itemSummaries.mapNotNull { summary ->

            val product = productsMap[summary.PRODUCT_ID] ?: return@mapNotNull null

            product.copy(
                selectedQty = summary.QUANTITY,
                TOTAL_QUANTITY = summary.AVAILABLE_QUANTITY,
                IS_BACK_ORDER = summary.IS_BACK_ORDER,
                MESSAGE = summary.MESSAGE,
                CHECKED = true,
            )
        }
    }

    fun mapItemsSummaryToRequest(
        itemSummaries: List<ItemsSummary>,
        selectedProducts: List<Product>
    ): Map<String, SaveOrderItemReq> {

        return itemSummaries.mapIndexed { index, summary ->

            val selectedQty =
                selectedProducts.getOrNull(index)?.selectedQty
                    ?: summary.REQUESTED_QUANTITY

            val quantity = if (!summary.IS_BACK_ORDER) {
                Log.d(
                    "WHATmaxOF1",
                    "summary.AVAILABLE_QUANTITY: ${summary.AVAILABLE_QUANTITY}, selectedQty: $selectedQty"
                )
//                minOf(summary.AVAILABLE_QUANTITY, selectedQty)
                selectedQty
            } else {
                Log.d(
                    "WHATmaxOF2",
                    "summary.AVAILABLE_QUANTITY: ${summary.AVAILABLE_QUANTITY}, selectedQty: $selectedQty"
                )
//                maxOf(0, selectedQty)
                selectedQty
            }

            index.toString() to SaveOrderItemReq(
                inventoryItemId = summary.PRODUCT_ID.toInt(),
                quantity = quantity
            )
        }.toMap()
    }
}