package com.akhnaton.foodvisits.data.model

data class VisitsPlaneDataDumy(

    val status: Int,
    val data: VisitsData
)

data class VisitsData(
    val user_order_type: Array<String>
)


data class VisitsCustomerType(
    val status: Int,
    val data: VisitsCustomerData
)


data class VisitsCustomerData(
    val user_customer_type: List<CustomerType>
)

data class CustomerType(
    val customer_type_id: String,
    val customer_name: String
)