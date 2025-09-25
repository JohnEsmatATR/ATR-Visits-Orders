package com.akhnaton.foodvisits.data.statusValue.customerCoding

import com.akhnaton.foodvisits.data.model.AreasResponse
import com.akhnaton.foodvisits.data.model.GovernoratesResponse

sealed class GetGovernoratesState {
    object Idle : GetGovernoratesState()
    object Loading : GetGovernoratesState()
    data class GovernoratesSuccess(val data: GovernoratesResponse) : GetGovernoratesState()
    data class AreasSuccess(val data: AreasResponse) : GetGovernoratesState()
    data class Error(val message: String) : GetGovernoratesState()

    val isLoading get() = this is Loading
    val isSuccess get() = this is GovernoratesSuccess || this is AreasSuccess
}
