package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IPromoter
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part

class PromoterRepository {
    private val retrofit = RetrofitClient.getInstance(IPromoter::class.java)

    suspend fun getItems(
        items: String,
        createdBy: String,
        customerCode: String,
        partySiteId: String
    ) =
        retrofit.getItems(
            items,
            createdBy,
            customerCode,
            partySiteId
        )


    suspend fun submitItems(
        createdBy: String,
        partySite: String,
        customerCode: String,
        date: String,
        itemId: String,
        quantity: String,
        returnQuantity: String,
        price: String,
        funNum: String,
    ) =
        retrofit.submitStock(
            createdBy,
            partySite,
            customerCode,
            date,
            itemId,
            quantity,
            returnQuantity,
            price,
            funNum
        )


    suspend fun getCurrentStockItems(
        employeeId: String,
        partySite: String,
        code: String,
        creationDate: String,
        userType: String,
        funNum: String
    ) =
        retrofit.getCurrentStockItems(
            employeeId,
            partySite,
            code,
            creationDate,
            userType,
            funNum
        )

    suspend fun getPromotersTargetNotes(
        employeeId: String,
        userType: String,
        funNum: String
    ) =
        retrofit.getPromotersTargetNotes(
            employeeId,
            userType,
            funNum
        )

    suspend fun sendDetails(
        employeeId: String,
        date: String,
        partySite: String,
        code: String,
        customerAvg: String,
        customerCall: String,
        customerPositiveCall: String,
        customerPurchase: String,
        userType: String,
        funNum: String
    ) =
        retrofit.sendDetails(
            employeeId,
            date,
            partySite,
            code,
            customerAvg,
            customerCall,
            customerPositiveCall,
            customerPurchase,
            userType,
            funNum
        )

    suspend fun uploadImages(
        image: Array<MultipartBody.Part?>?,
        created_by: RequestBody,
        creation_date: RequestBody,
        customer_code: RequestBody,
        party_site_id: RequestBody,
        user_type: RequestBody,
        funNum: RequestBody
    ) =
        retrofit.uploadImages(
            image,
            created_by,
            creation_date,
            customer_code,
            party_site_id,
            user_type,
            funNum
        )

}