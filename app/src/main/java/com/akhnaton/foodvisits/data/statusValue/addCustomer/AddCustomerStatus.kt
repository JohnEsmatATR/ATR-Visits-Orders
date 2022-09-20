package com.akhnaton.foodvisits.data.statusValue.addCustomer

import com.akhnaton.foodvisits.data.model.CreateNewCustomer
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.visits.CustomerLines
import com.akhnaton.foodvisits.data.model.visits.CustomerSite
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus


sealed class AddCustomerStatus {

    object Idle : AddCustomerStatus()
    object Loading : AddCustomerStatus()
    data class GetCustomerType(val data: VisitsCustomerType) : AddCustomerStatus()
    data class GetLines(val data: Lines) : AddCustomerStatus()
    data class GetMainLine(val data: CustomerLines) : AddCustomerStatus()
    data class CreateCustomer(val data: CreateNewCustomer) : AddCustomerStatus()
    data class Error(val error: String?) : AddCustomerStatus()
}