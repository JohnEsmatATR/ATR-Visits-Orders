package com.akhnaton.foodvisits.data.model.bundleVisits

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("visits") val visits: List<BundleVisit>,
    @SerializedName("reject_reasons") val rejectReasons: List<RejectReason>
)

data class BundleVisit(
    @SerializedName("VISIT_DETAIL_ID") val visitDetailId: String,
    @SerializedName("CUSTOMER_PARTY_SITE_ID") val customerPartySiteId: String,
    @SerializedName("CUSTOMER_NAME") val customerName: String,
    @SerializedName("CUSTOMER_ADDRESS") val customerAddress: String,
    @SerializedName("CUSTOMER_CODE") val customerCode: String,
    @SerializedName("CUSTOMER_ORDER_TYPE") val customerOrderType: String,
    @SerializedName("CUSTOMER_LATITUDE") val customerLatitude: String?,
    @SerializedName("CUSTOMER_LONGITUDE") val customerLongitude: String?,
    @SerializedName("DATE_OF_VISIT") val dateOfVisit: String,
    @SerializedName("COUNT_OF_VISITS") val countOfVisits: String
) : java.io.Serializable

data class RejectReason(
    @SerializedName("ID") val id: String,
    @SerializedName("NAME") val name: String
) : java.io.Serializable