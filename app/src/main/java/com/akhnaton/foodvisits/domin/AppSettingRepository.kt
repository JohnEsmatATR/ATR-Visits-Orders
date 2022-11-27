package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IAppSetting
import com.akhnaton.foodvisits.shared.RetrofitClient

class AppSettingRepository {
    private val retrofit = RetrofitClient.getInstance(IAppSetting::class.java)

    suspend fun getAppSetting(appVersion: String) = retrofit.getAppSetting(appVersion)

}