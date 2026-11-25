package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IBundleVisits
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BundleVisitsRepository {

    private val retrofit = RetrofitClient.getInstance(IBundleVisits::class.java)

    suspend fun searchCustomer(code: String) = retrofit.searchCustomer(code)

    suspend fun getRejectReasons() = retrofit.getRejectReasons()

    suspend fun saveDone(
        bundleImage: MultipartBody.Part?,
        isCoded: RequestBody,
        custCode: RequestBody?,
        customerName: RequestBody?,
        customerAddress: RequestBody?,
        takesBundle: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
    ) = retrofit.saveDone(
        bundleImage, isCoded, custCode, customerName, customerAddress, takesBundle, latitude, longitude
    )
    suspend fun saveReject(req: BundleRejectReq) = retrofit.saveReject(req)
}