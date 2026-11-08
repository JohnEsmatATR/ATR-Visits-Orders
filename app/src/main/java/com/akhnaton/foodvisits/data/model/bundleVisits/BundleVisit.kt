package com.akhnaton.foodvisits.data.model.bundleVisits

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("visits") val visits: List<BundleVisit>,
    @SerializedName("reject_reasons") val rejectReasons: List<RejectReason>
)

data class BundleVisit(
    @SerializedName("VISIT_ID") val visitId: String,
    @SerializedName("CUST_ID") val custId: String,
    @SerializedName("PERSON_ID") val personId: String,
    @SerializedName("VISIT_DATE") val visitDate: String,
    @SerializedName("SUPER_ID") val superId: String,
    @SerializedName("STATUS") val status: Int,
    @SerializedName("REASON_ID") val reasonId: String,
    @SerializedName("CUSTNAME") val custName: String,
    @SerializedName("CUST_ADDRESS") val custAddress: String,
    @SerializedName("DISTRICT") val district: String,
    @SerializedName("LATITUDE") val latitude: String,
    @SerializedName("LONGITUDE") val longitude: String,
    @SerializedName("IMAGES_PATH") val imagesPath: String
) : java.io.Serializable

data class RejectReason(
    @SerializedName("ID") val id: String,
    @SerializedName("NAME") val name: String
) : java.io.Serializable