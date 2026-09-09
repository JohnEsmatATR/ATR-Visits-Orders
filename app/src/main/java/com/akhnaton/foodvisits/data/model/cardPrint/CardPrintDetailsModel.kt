package com.akhnaton.foodvisits.data.model.cardPrint

data class CardPrintDetailsModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: CardPrintDetailsData
)

data class CardPrintDetailsData(
    val invoice_info: InvoiceInfo,
    val invoice_details: List<InvoiceDetailItem>
)

data class InvoiceInfo(
    val customer_name: String,
    val customer_address: String,
    val invoice_total_value: Double,
    val payment_method: String
)

data class InvoiceDetailItem(
    val tax_value: Double,
    val unit_selling_price: Double,
    val order_quantity: Int,
    val unit_total_value: Double,
    val item_desc: String,
    val lot_number: String
)