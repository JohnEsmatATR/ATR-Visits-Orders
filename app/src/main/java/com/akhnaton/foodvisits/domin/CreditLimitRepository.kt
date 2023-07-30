package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ISupervisor
import com.akhnaton.foodvisits.shared.RetrofitClient
import retrofit2.http.Field

class CreditLimitRepository {
    private val retrofit = RetrofitClient.getInstance(ISupervisor::class.java)

    suspend fun getCreditLimit(
        app_version: String?,
        api_token: String?,
        orderNumber: String?,
        customer_id: String?,
        order_total_price: String?,
    ) = retrofit.getCreditLimit(app_version, api_token, orderNumber, customer_id, order_total_price)

    suspend fun sendCreditLimit(
        app_version: String,
        api_token: String,
        customer_code: String,
        customer_name: String,
        customer_description: String,
        customer_branch: String,
        branch: String,
        customer_method_payment: String,
        customer_current_opening_status: String,
        customer_commercial_register: String,
        customer_previous_credit_limit: String,
        customer_quarter_plan: String,
        sell_client_to_date: String,
        investigation_ratio: String,
        customer_required_credit: String,
        customer_number_branches: String,
        customer_guarantee: String,
        customer_current_limit: String,
        customer_withdrwals_current_year: String,
        customer_withdrwals_last_year: String,
        customer_bounces_current_year_forward_transaction: String,
        customer_overrun_money: String,
        customer_insurance: String,
        customer_entered_orders: String,
        customer_booked_orders: String,
        customer_check_remitted_amount: String,
        customer_total_limit: String,
        remaining_credit: String,
        customer_national_id: String,
        customer_security_cheques: String,
        order_number: String,
    ) = retrofit.sendCreditLimitForm(
        app_version,
        api_token,
        customer_code,
        customer_name,
        customer_description,
        customer_branch,
        branch,
        customer_method_payment,
        customer_current_opening_status,
        customer_commercial_register,
        customer_previous_credit_limit,
        customer_quarter_plan,
        sell_client_to_date,
        investigation_ratio,
        customer_required_credit,
        customer_number_branches,
        customer_guarantee,
        customer_current_limit,
        customer_withdrwals_current_year,
        customer_withdrwals_last_year,
        customer_bounces_current_year_forward_transaction,
        customer_overrun_money,
        customer_insurance,
        customer_entered_orders,
        customer_booked_orders,
        customer_check_remitted_amount,
        customer_total_limit,
        remaining_credit,
        customer_national_id,
        customer_security_cheques,
        order_number,
    )
}