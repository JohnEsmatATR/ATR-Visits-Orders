package com.akhnaton.foodvisits.data.model.supervisor.creditDetails

data class CreditLimitDetails (
    val status: Int = 0,
    val message: String? = null,
    val data: CreditLimitDetailsModel? = null
)

data class CreditLimitDetailsModel (
    val customer_code: String? = null,
    val customer_name: String? = null,
    val customer_description: String? = null,
    val customer_branch: String? = null,
    val branch: String? = null,
    val customer_method_payment: String? = null,
    val customer_current_opening_status: String? = null,
    val customer_commercial_register: String? = null,
    val customer_previous_credit_limit: String? = null,
    val customer_quarter_plan: String? = null,
    val sell_client_to_date: String? = null,
    val investigation_ratio: String? = null,
    val customer_required_credit: String? = null,
    val customer_number_branches: String? = null,
    val customer_guarantee: String? = null,
    val customer_current_limit: String? = null,
    val customer_withdrwals_current_year: String? = null,
    val customer_withdrwals_last_year: String? = null,
    val customer_bounces_current_year_forward_transaction: String? = null,
    val customer_overrun_money: String? = null,
    val customer_insurance: String? = null,
    val customer_entered_orders: String? = null,
    val customer_booked_orders: String? = null,
    val customer_check_remitted_amount: String? = null,
    val customer_total_limit: String? = null,
    val remaining_credit: String? = null,
    val customer_national_id: String? = null,
    val customer_security_cheques: String? = null,
)