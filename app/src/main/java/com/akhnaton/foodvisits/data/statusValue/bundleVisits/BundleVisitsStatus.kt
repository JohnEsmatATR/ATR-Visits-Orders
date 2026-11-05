package com.akhnaton.foodvisits.data.statusValue.bundlevisits

import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisitsRes
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes

sealed class BundleVisitsStatus {
    object Idle : BundleVisitsStatus()
    object Loading : BundleVisitsStatus()
    data class GetTodayVisits(val data: BundleVisitsRes) : BundleVisitsStatus()
    data class SaveDone(val data: BundleVisitsRes) : BundleVisitsStatus()
    data class SaveReject(val data: BundleVisitsRes) : BundleVisitsStatus()
    data class RefreshToken(val data: RefreshTokenRes) : BundleVisitsStatus()
    data class Error(val error: String?) : BundleVisitsStatus()
}