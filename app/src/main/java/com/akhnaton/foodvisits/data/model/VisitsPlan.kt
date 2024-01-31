package com.akhnaton.foodvisits.data.model

import androidx.room.*
import java.io.Serializable

@Entity(tableName = "visits_plan")
data class VisitsPlan(
    @PrimaryKey
    var id: Int,
    var status: Int,
    @TypeConverters(VisitPlanDataConverters::class)
    @Embedded var data: VisitPlanData,
) {
    constructor(): this(0, 0, VisitPlanData(listOf(),"",""))
}

data class VisitPlanData(
    @TypeConverters(ListVisitPlanConverters::class)
    var customer_visit_plan: List<CustomerVisitPlan>,
    var date: String,
    var day: String,
) {
    constructor(): this(listOf(),"","")
}

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
    var is_visited_today: Boolean,
    var CUSTOMER_CODE: String,
) : Serializable {
    constructor(): this("", "", "", "", "" , "","", "", false, "")
}