package com.akhnaton.foodvisits.data.statusValue.appSetting

sealed class AppSettingIntent {

    data class GetAppSetting(
        val app_version: String
    ) : AppSettingIntent()
}