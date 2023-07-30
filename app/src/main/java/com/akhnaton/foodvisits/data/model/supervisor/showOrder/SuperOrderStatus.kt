package com.akhnaton.foodvisits.data.model.supervisor.showOrder

data class SuperOrderStatus(
    var up_dat: String,
    var nomorder: String,
    var line_name: String,
    var custname: String,
    var customer_code: String,
    var customer_id: String,
    var salesrep: String,
    var telesales: String,
    var supervisor: String,
    var rets: String,
    var comment: String,
    var pay_nm: String,
    var ord_type: String,
    var super_approval: String,
    var total: String,
    var approve_stat_quota: String,
    var quota_flag: String,
    var rets_name: String
)