package com.akhnaton.foodvisits.data.statusValue.supervisor.creditDetails

sealed class CreditLimitIntent {

    data class GetCreditLimit(
        val app_version: String,
        val api_token: String,
        val orderNumber: String,
        val customer_id: String,
        val order_total_price: String,
    ) : CreditLimitIntent()

    data class SendCreditLimit(
        val app_version: String,
        val api_token: String,
        val customer_code: String,
        val customer_name: String,
        val customer_description: String,
        val customer_branch: String,
        val branch: String,
        val customer_method_payment: String,
        val customer_current_opening_status: String,
        val customer_commercial_register: String,
        val customer_previous_credit_limit: String,
        val customer_quarter_plan: String,
        val sell_client_to_date: String,
        val investigation_ratio: String,
        val customer_required_credit: String,
        val customer_number_branches: String,
        val customer_guarantee: String,
        val customer_current_limit: String,
        val customer_withdrwals_current_year: String,
        val customer_withdrwals_last_year: String,
        val customer_bounces_current_year_forward_transaction: String,
        val customer_overrun_money: String,
        val customer_insurance: String,
        val customer_entered_orders: String,
        val customer_booked_orders: String,
        val customer_check_remitted_amount: String,
        val customer_total_limit: String,
        val remaining_credit: String,
        val customer_national_id: String,
        val customer_security_cheques: String,
        val order_number: String,
    ) : CreditLimitIntent()

}