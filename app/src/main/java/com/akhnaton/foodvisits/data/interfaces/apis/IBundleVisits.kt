package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisitsRes
import com.akhnaton.foodvisits.shared.ConstantLinks
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_TODAY_UNIFIED_VISITS
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_UNIFIED_VISIT_RESULT
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface IBundleVisits {

    @GET(ConstantLinks.SEARCH_CUSTOMERS)
    suspend fun searchCustomer(
        @Query("customer_code") code: String
    ): BundleVisitsRes

    @GET(ConstantLinks.GET_BUNDLE_REJECT_REASONS)
    suspend fun getRejectReasons(): BundleVisitsRes

    @Multipart
    @POST(ConstantLinks.SAVE_BUNDLE_VISIT)
    suspend fun saveDone(
        @Part bundleImage: MultipartBody.Part,
        @Part("is_coded") isCoded: RequestBody,
        @Part("cust_code") custCode: RequestBody?,
        @Part("takes_bundle") takesBundle: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
    ): BundleVisitsRes

    @POST(ConstantLinks.SAVE_BUNDLE_VISIT)
    suspend fun saveReject(
        @Body req: BundleRejectReq
    ): BundleVisitsRes
}