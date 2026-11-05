package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IBundleVisits
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BundleVisitsRepository {

    private val retrofit = RetrofitClient.getInstance(IBundleVisits::class.java)

    suspend fun getTodayVisits() = retrofit.getTodayVisits()

    suspend fun saveDone(
        photo: MultipartBody.Part,
        visitId: RequestBody,
        status: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
    ) = retrofit.saveDone(photo, visitId, status, latitude, longitude)

    suspend fun saveReject(req: BundleRejectReq) = retrofit.saveReject(req)
}