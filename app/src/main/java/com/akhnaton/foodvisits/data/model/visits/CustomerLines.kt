package com.akhnaton.foodvisits.data.model.visits

data class CustomerLines(
    val status: Int,
    val data: CustomerLinesData
)


data class CustomerLinesData(
    val main_customer_line: List<MainCustomerLine>
)


data class MainCustomerLine(
    val customer_name: String,
    val customer_code: String
)