package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IBase {
    @FormUrlEncoded
    @POST(ConstantLinks.REFRESH_TOKEN)
    suspend fun refreshToken(
        @Field("USER_ID") userId: String,
        @Field("TOKEN") token: String
    ): RefreshTokenRes
}