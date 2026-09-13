package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ICustomer
import com.akhnaton.foodvisits.data.model.personCoding.AddCustomerModel
import com.akhnaton.foodvisits.data.model.personCoding.AreasModel
import com.akhnaton.foodvisits.data.model.personCoding.GovernoratesModel
import com.akhnaton.foodvisits.data.model.personCoding.LinesModel
import com.akhnaton.foodvisits.data.model.personCoding.MainCustomersLineModel
import com.akhnaton.foodvisits.data.model.personCoding.SalesAndCustomerModel
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PersonCodingRepository {

    private val api = RetrofitClient.getInstance(ICustomer::class.java)

    suspend fun getSalesAndCustomerTypes(): SalesAndCustomerModel {
        return api.getSalesAndCustomerTypes()
    }

    suspend fun getLines(saleType: String, customerType: String): LinesModel {
        return api.getLines(saleType, customerType)
    }

    suspend fun getMainCustomersLine(
        lineId: String,
        orderType: String,
        customerType: String
    ): MainCustomersLineModel {
        return api.getMainCustomersLine(lineId, orderType, customerType)
    }

    suspend fun getUserAreas(): GovernoratesModel {
        return api.getUserAreas()
    }
    suspend fun getAreasByGovernorate(governorateId: String): AreasModel {
        return api.getAreasByGovernorate(governorateId)
    }

    suspend fun addCustomer(
        fields: Map<String, RequestBody>,
        frontImage: MultipartBody.Part?,
        backImage: MultipartBody.Part?
    ): AddCustomerModel {
        return api.addCustomer(fields, frontImage, backImage)
    }

}