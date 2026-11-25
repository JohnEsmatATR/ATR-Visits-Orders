package com.akhnaton.foodvisits.data.model.bundleVisits

import com.google.gson.annotations.SerializedName

data class MyCustomersData(
    @SerializedName("customers") val customers: List<MyCustomer>
)

data class MyCustomer(
    @SerializedName("CUSTOMER_CODE") val customerCode: String,
    @SerializedName("CUSTOMER_NAME") val customerName: String,
    @SerializedName("SITE_ADDRESS") val siteAddress: String,
    @SerializedName("TEAM_NAME") val teamName: String,
    @SerializedName("SALE_TYPE") val saleType: String,
    @SerializedName("LINE_CODE") val lineCode: String,
    @SerializedName("LINE_NAME") val lineName: String,
    @SerializedName("SALE_CHILD_NAME") val saleChildName: String,
    @SerializedName("SALES_MAN") val salesMan: String,
    @SerializedName("ASSIGNED_SALESMAN_ID") val assignedSalesmanId: String
)

data class SearchCustomerData(
    @SerializedName("customer_code") val customerCode: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("customer_address") val customerAddress: String,
    @SerializedName("team_name") val teamName: String,
    @SerializedName("line_code") val lineCode: String,
    @SerializedName("line_name") val lineName: String,
    @SerializedName("sale_type") val saleType: String,
    @SerializedName("sale_child_name") val saleChildName: String,
    @SerializedName("sales_man") val salesMan: String,
    @SerializedName("assigned_sales_id") val assignedSalesId: String,
    @SerializedName("is_assigned_to_me") val isAssignedToMe: Boolean
)

data class ReasonsData(
    @SerializedName("reasons") val reasons: List<RejectReason>
)

data class SaveVisitData(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("customer_code") val customerCode: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("customer_address") val customerAddress: String,
    @SerializedName("status") val status: String,
    @SerializedName("reject_reason_id") val rejectReasonId: String?
)