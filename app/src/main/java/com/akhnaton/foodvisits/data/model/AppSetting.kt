package com.akhnaton.foodvisits.data.model

data class AppSetting(
    var data: AppSettingData
)

data class AppSettingData(
    var lowest_price_order: Int,
    var limit_area: Int
)