package com.akhnaton.foodvisits.data.statusValue.personCoding

import com.akhnaton.foodvisits.data.model.personCoding.AddCustomerModel
import com.akhnaton.foodvisits.data.model.personCoding.AreasModel
import com.akhnaton.foodvisits.data.model.personCoding.GovernoratesModel
import com.akhnaton.foodvisits.data.model.personCoding.LinesModel
import com.akhnaton.foodvisits.data.model.personCoding.MainCustomersLineModel
import com.akhnaton.foodvisits.data.model.personCoding.SalesAndCustomerModel
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes

sealed class PersonStatus {
    object Loading : PersonStatus()
    data class GetSalesAndCustomerTypes(val response: SalesAndCustomerModel) : PersonStatus()
    data class GetLines(val response: LinesModel) : PersonStatus()
    data class GetMainCustomersLine(val response: MainCustomersLineModel) : PersonStatus()
    data class RefreshToken(val data: RefreshTokenRes) : PersonStatus()
    data class Error(val message: String?) : PersonStatus()
    data class GetUserAreas(val response: GovernoratesModel) : PersonStatus()
    data class GetAreasByGovernorate(val response: AreasModel) : PersonStatus()
    data class AddCustomer(val response: AddCustomerModel) : PersonStatus()
}