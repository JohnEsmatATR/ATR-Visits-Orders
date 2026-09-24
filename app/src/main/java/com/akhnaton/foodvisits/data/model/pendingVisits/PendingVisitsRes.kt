package com.akhnaton.foodvisits.data.model.pendingVisits

import com.google.gson.JsonElement

data class PendingVisitsRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)

data class PendingVisitsData(
    val visits: List<PendingVisitItem>,
    val pagination: PendingVisitsPagination
)

data class PendingVisitItem(
    val ID: String,
    val REF_ID: String?,
    val APPROVE_FLAG: String?,
    val LAST_NAME: String,
    val PARTY_SITE_ID: String,
    val CUSTOMER_NAME: String,
    val DATE_OF_VISIT: String
)

data class PendingVisitsPagination(
    val current_page: Int,
    val per_page: Int,
    val total_rows: Int
)

data class ApproveVisitsReq(
    val ids: List<String>,
    val decision: Int
)

data class ApproveVisitsRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)