package com.akhnaton.foodvisits.data.model
import java.io.Serializable

data class VisitsPlan(
    val status: Int,
    val data: VisitsPlanData
)

data class VisitsPlanData(
    var customer_visit_plan: List<CustomerVisitPlan>
)

@kotlinx.serialization.Serializable
class CustomerVisitPlan(
    var customer_party_site_id: String,
    var customer_name: String,
    var customer_type: String,
    var customer_order_type: String,
    var customer_line_id: String,
    var customer_latitude: String,
    var customer_longitude: String,
    var customer_address: String,
): Serializable