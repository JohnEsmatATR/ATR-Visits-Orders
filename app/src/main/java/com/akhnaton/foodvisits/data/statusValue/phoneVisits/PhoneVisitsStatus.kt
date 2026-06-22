package com.akhnaton.foodvisits.data.statusValue.phoneVisits

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.VisitsPlaneDataDumy
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.customers.GetCustomersRes
import com.akhnaton.foodvisits.data.model.getCustomerData.GetCustomerDataRes
import com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.GetSalesAndCustomerTypesRes
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneRes
import com.akhnaton.foodvisits.data.model.visits.CustomerLines
import com.akhnaton.foodvisits.data.model.visits.CustomerSite
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit
import com.akhnaton.foodvisits.data.statusValue.order.OrderStatus


sealed class PhoneVisitsStatus {

    object Idle : PhoneVisitsStatus()
    object Loading : PhoneVisitsStatus()
    data class GetSalesAndCustomerTypes(val data: GetSalesAndCustomerTypesRes) : PhoneVisitsStatus()
    data class GetCustomers(val data: GetCustomersRes) : PhoneVisitsStatus()
    data class GetCustomerData(val data: GetCustomerDataRes) : PhoneVisitsStatus()
    data class SaveVisitPhone(val data: SaveVisitPhoneRes) : PhoneVisitsStatus()
    data class RefreshToken(val data: RefreshTokenRes) : PhoneVisitsStatus()

    data class Plan(val data: VisitsPlaneDataDumy) : PhoneVisitsStatus()
    data class GetCustomerType(val data: VisitsCustomerType) : PhoneVisitsStatus()
    data class GetLines(val data: Lines) : PhoneVisitsStatus()
    data class GetCustomerLines(val data: CustomerLines) : PhoneVisitsStatus()
    data class GetCustomersSite(val data: CustomerSite) : PhoneVisitsStatus()
    data class SavePhoneVisits(val data: SaveVisit) : PhoneVisitsStatus()
    data class GetAppSetting(val data: AppSetting) : PhoneVisitsStatus()
    data class Error(val error: String?) : PhoneVisitsStatus()
}