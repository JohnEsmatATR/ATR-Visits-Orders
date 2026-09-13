package com.akhnaton.foodvisits.data.model.personCoding

data class SalesAndCustomerModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: SalesAndCustomerTypesData?
)

data class SalesAndCustomerTypesData(
    val sales_types: List<String>?,
    val customer_types: List<CustomerType>?
)

data class CustomerType(
    val TYPE_CHILD_CODE: String,
    val TYPE_CHILD_NAME: String
) {
    override fun toString(): String = TYPE_CHILD_NAME
}