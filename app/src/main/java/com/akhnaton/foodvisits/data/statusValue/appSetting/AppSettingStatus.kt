package com.akhnaton.foodvisits.data.statusValue.appSetting

import com.akhnaton.foodvisits.data.model.AppSetting

sealed class AppSettingStatus {

    object Idle : AppSettingStatus()
    object Loading : AppSettingStatus()
    data class GetAppSetting(val data: AppSetting) : AppSettingStatus()
    data class Error(val error: String?) : AppSettingStatus()
}