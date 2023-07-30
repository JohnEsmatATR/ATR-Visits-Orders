package com.akhnaton.foodvisits.ui.home.supervisor.creditLimit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.supervisor.creditDetails.CreditLimitState
import com.akhnaton.foodvisits.domin.CreditLimitRepository
import com.akhnaton.foodvisits.data.statusValue.supervisor.creditDetails.CreditLimitIntent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class CreditLimitViewModel : ViewModel() {
    val creditLimitIntent = Channel<CreditLimitIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<CreditLimitState>(CreditLimitState.Idle)
    val state: StateFlow<CreditLimitState> get() = _state

    init {
        creditLimit()
    }

    private fun creditLimit() {
        viewModelScope.launch {
            creditLimitIntent.consumeAsFlow().collect {
                when (it) {
                    is CreditLimitIntent.GetCreditLimit -> getCreditLimitRepo(
                        it.app_version,
                        it.api_token,
                        it.orderNumber,
                        it.customer_id,
                        it.order_total_price,
                    )

                    is CreditLimitIntent.SendCreditLimit -> sendCreditLimitRepo(
                        it.app_version,
                        it.api_token,
                        it.customer_code,
                        it.customer_name,
                        it.customer_description,
                        it.customer_branch,
                        it.branch,
                        it.customer_method_payment,
                        it.customer_current_opening_status,
                        it.customer_commercial_register,
                        it.customer_previous_credit_limit,
                        it.customer_quarter_plan,
                        it.sell_client_to_date,
                        it.investigation_ratio,
                        it.customer_required_credit,
                        it.customer_number_branches,
                        it.customer_guarantee,
                        it.customer_current_limit,
                        it.customer_withdrwals_current_year,
                        it.customer_withdrwals_last_year,
                        it.customer_bounces_current_year_forward_transaction,
                        it.customer_overrun_money,
                        it.customer_insurance,
                        it.customer_entered_orders,
                        it.customer_booked_orders,
                        it.customer_check_remitted_amount,
                        it.customer_total_limit,
                        it.remaining_credit,
                        it.customer_national_id,
                        it.customer_security_cheques,
                        it.order_number,
                    )
                }
            }
        }
    }

    private fun getCreditLimitRepo(
        app_version: String,
        api_token: String,
        orderNumber: String,
        customer_id: String,
        order_total_price: String,
    ) {
        viewModelScope.launch {
            _state.value = CreditLimitState.Loading
            _state.value = try {
                CreditLimitState.GetCreditLimit(
                    CreditLimitRepository().getCreditLimit(
                        app_version,
                        api_token,
                        orderNumber,
                        customer_id,
                        order_total_price,
                    )!!
                )
            } catch (e: Exception) {
                CreditLimitState.Error(e.message)
            }
        }
    }

    private fun sendCreditLimitRepo(
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
    ) {
        viewModelScope.launch {
            _state.value = CreditLimitState.Loading
            _state.value = try {
                CreditLimitState.SendCreditLimit(
                    CreditLimitRepository().sendCreditLimit(
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
                    )!!
                )
            } catch (e: Exception) {
                CreditLimitState.Error(e.message)
            }
        }
    }

    companion object {
        private const val TAG = "CreditLimitViewModel"
    }
}