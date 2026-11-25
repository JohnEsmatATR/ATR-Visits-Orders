package com.akhnaton.foodvisits.data.statusValue.bundlevisits

import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import okhttp3.MultipartBody
import okhttp3.RequestBody

sealed class BundleVisitsIntent {

    data class SearchCustomer(val code: String) : BundleVisitsIntent()

    object GetRejectReasons : BundleVisitsIntent()

    data class SaveDone(
        val photo: MultipartBody.Part?,
        val isCoded: RequestBody,
        val custCode: RequestBody?,
        val customerName: RequestBody?,
        val customerAddress: RequestBody?,
        val takesBundle: RequestBody,
        val latitude: RequestBody,
        val longitude: RequestBody,
    ) : BundleVisitsIntent()

    data class SaveReject(val req: BundleRejectReq) : BundleVisitsIntent()

    data class RefreshToken(val userId: String, val token: String) : BundleVisitsIntent()
}