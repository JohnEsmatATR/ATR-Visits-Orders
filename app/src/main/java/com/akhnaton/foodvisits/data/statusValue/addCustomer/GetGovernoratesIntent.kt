package com.akhnaton.foodvisits.data.statusValue.addCustomer

sealed class GetGovernoratesIntent {
    data class GetGovernorate(val version : String, val token : String) : GetGovernoratesIntent()
    data class GetCity(val version : String, val token : String,val governorateId: String) : GetGovernoratesIntent()
}