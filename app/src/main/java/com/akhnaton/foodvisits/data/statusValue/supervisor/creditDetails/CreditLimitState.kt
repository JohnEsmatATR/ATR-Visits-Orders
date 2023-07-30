package com.akhnaton.foodvisits.data.statusValue.supervisor.creditDetails

import com.akhnaton.foodvisits.data.model.supervisor.StaticResponse
import com.akhnaton.foodvisits.data.model.supervisor.creditDetails.CreditLimitDetails


sealed class CreditLimitState {

    object Idle : CreditLimitState()
    object Loading : CreditLimitState()
    data class GetCreditLimit(val creditLimitDetails: CreditLimitDetails) : CreditLimitState()
    data class SendCreditLimit(val staticResponse: StaticResponse) : CreditLimitState()
    data class Error(val error: String?) : CreditLimitState()
}