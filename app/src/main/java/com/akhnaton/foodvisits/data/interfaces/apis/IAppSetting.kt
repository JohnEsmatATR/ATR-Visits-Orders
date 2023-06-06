package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IAppSetting {

    @FormUrlEncoded
    @POST(ConstantLinks.APP_SETTING)
    suspend fun getAppSetting(
        @Field("app_version") appVersion: String
    ): AppSetting
}