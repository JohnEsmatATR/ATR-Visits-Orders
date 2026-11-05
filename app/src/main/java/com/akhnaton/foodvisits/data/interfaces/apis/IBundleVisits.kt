package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisitsRes
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_TODAY_UNIFIED_VISITS
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_UNIFIED_VISIT_RESULT
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface IBundleVisits {

    @GET(GET_TODAY_UNIFIED_VISITS)
    suspend fun getTodayVisits(): BundleVisitsRes

    @Multipart
    @POST(SAVE_UNIFIED_VISIT_RESULT)
    suspend fun saveDone(
        @Part photo: MultipartBody.Part,
        @Part("visit_id") visitId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
    ): BundleVisitsRes

    @POST(SAVE_UNIFIED_VISIT_RESULT)
    suspend fun saveReject(
        @Body req: BundleRejectReq
    ): BundleVisitsRes
}