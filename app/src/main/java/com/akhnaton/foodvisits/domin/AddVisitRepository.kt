package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IAddVisit
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRequest
import com.akhnaton.foodvisits.shared.RetrofitClient

class AddVisitRepository {

    private val retrofit = RetrofitClient.getInstance(IAddVisit::class.java)

    suspend fun getSalesAndCustomerTypes() = retrofit.getSalesAndCustomerTypes()

    suspend fun getLines(saleType: String) = retrofit.getLines(saleType)

    suspend fun getVisitCustomers(lineId: String) = retrofit.getVisitCustomers(lineId)

    suspend fun saveSetupPlan(request: SaveSetupPlanRequest) = retrofit.saveSetupPlan(request)

}