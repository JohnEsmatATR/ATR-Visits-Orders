package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IPromoter
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PromoterRepository {
    private val retrofit = RetrofitClient.getInstance(IPromoter::class.java)


    suspend fun submitItems(
        appVersion: Double?,
        apiToken: String?,
        createdBy: Int?,
        partySite: Int?,
        date: String?,
        itemId: Int?,
        returnQuantity: Int?,
        quantity: Int?,
        price: Double?,
        customerCode: Int?,
        userType: String?,
    ) =
        retrofit.submitStock(
            appVersion,
            apiToken,
            createdBy,
            partySite,
            date,
            itemId,
            returnQuantity,
            quantity,
            price,
            customerCode,
            userType,
        )


    suspend fun getCurrentStockItems(
        appVersion: Double,
        apiToken: String,
        createdBy: Int,
        partySiteId: Int,
        customerCode: Int,
        creationDate: String,
        items: Int,
    ) =
        retrofit.getCurrentStockItems(
            appVersion,
            apiToken,
            createdBy,
            partySiteId,
            customerCode,
            creationDate,
            items,
        )

//    suspend fun getPromotersTargetNotes(
//        employeeId: String,
//        userType: String,
//        funNum: String
//    ) =
//        retrofit.getPromotersTargetNotes(
//            employeeId,
//            userType,
//            funNum
//        )

    suspend fun sendDetails(
        appVersion: Double,
        apiToken: String,
        employeeId: Int,
        date: String,
        partySite: Int,
        code: Int,
        customerAvg: Int,
        customerCall: Int,
        customerPositiveCall: Int,
        customerPurchase: Int,
        userType: String,
        funNum: Int
    ) =
        retrofit.sendDetails(
            appVersion,
            apiToken,
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
        appVersion: RequestBody?,
        apiToken: RequestBody?,
        image: Array<MultipartBody.Part?>?,
        created_by: RequestBody?,
        creation_date: RequestBody?,
        customer_code: RequestBody?,
        party_site_id: RequestBody?,
        user_type: RequestBody?,
        funNum: RequestBody?,
    ) =
        retrofit.uploadImages(
            appVersion,
            apiToken,
            image,
            created_by,
            creation_date,
            customer_code,
            party_site_id,
            user_type,
            funNum
        )


    suspend fun sendCompetitors(
        appVersion: RequestBody,
        apiToken: RequestBody,
        image: Array<MultipartBody.Part?>,
        created_by: RequestBody,
        creation_date: RequestBody,
        party_site_id: RequestBody,
        customer_code: RequestBody,
        product_id: RequestBody,
        price: RequestBody,
        price_after_disc: RequestBody,
        product_name: RequestBody,
        weight: RequestBody,
        discount_rate: RequestBody,
        prom_type: RequestBody,
        prom_date: RequestBody,
        user_type: RequestBody,
        PromoterCompetitorCompress: RequestBody,
    ) =
        retrofit.sendCompetitors(
            appVersion,
            apiToken,
            image,
            created_by,
            creation_date,
            party_site_id,
            customer_code,
            product_id,
            price,
            price_after_disc,
            product_name,
            weight,
            discount_rate,
            prom_type,
            prom_date,
            user_type,
            PromoterCompetitorCompress,
        )

}