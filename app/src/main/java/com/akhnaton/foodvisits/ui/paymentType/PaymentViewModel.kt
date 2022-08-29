package com.akhnaton.foodvisits.ui.paymentType

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.payment.PaymentIntent
import com.akhnaton.foodvisits.data.statusValue.payment.PaymentStatus
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.domin.PaymentRepository
import com.akhnaton.foodvisits.domin.VisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {


    val paymentIntent = Channel<PaymentIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)

    val status: StateFlow<PaymentStatus> get() = _status

    init {
        getPaymentPlan()
    }

    private fun getPaymentPlan() {
        viewModelScope.launch {
            paymentIntent.consumeAsFlow().collect {
                when (it) {
                    is PaymentIntent.Payments -> fetchPayments(
                        it.app_version,
                        it.api_token,
                        it.customerPartySiteId,
                        it.orderType,
                        it.customerType
                    )
                }
            }
        }
    }

    private fun fetchPayments(
        appVersion: String,
        apiToken: String,
        customerPartySiteId: String,
        orderType: String,
        customerType: String
    ) {
        viewModelScope.launch {
            _status.value = PaymentStatus.Idle
            _status.value = try {
                PaymentStatus.GetPayments(
                    PaymentRepository().getPayment(
                        appVersion,
                        apiToken,
                        customerPartySiteId,
                        orderType,
                        customerType
                    )
                )
            } catch (e: Exception) {
                PaymentStatus.Error(e.message)
            }
        }
    }


}