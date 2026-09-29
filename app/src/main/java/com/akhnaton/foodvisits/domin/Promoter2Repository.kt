package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IPromoter2
import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class Promoter2Repository {
    private val retrofit = RetrofitClient.getInstance(IPromoter2::class.java)

    suspend fun checkInGPS(checkInGPSReq: CheckInGPSReq) =
        retrofit.checkInGPS(checkInGPSReq)

    suspend fun promoterGetItemData(
        customerCode: String,
        partySiteId: String
    ) =
        retrofit.promoterGetItemData(
            customerCode,
            partySiteId
        )

    suspend fun promoterSaveStock(
        promoterSaveStockReq: PromoterSaveStockReq
    ) =
        retrofit.promoterSaveStock(
            promoterSaveStockReq
        )

    suspend fun getCompetitorList(
        appVersion: Double,
    ) = retrofit.getCompetitorList(
        appVersion,
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
        image: MultipartBody.Part,
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
        competitor_name: RequestBody,
        type_name: RequestBody,
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
            competitor_name,
            type_name,
        )

}