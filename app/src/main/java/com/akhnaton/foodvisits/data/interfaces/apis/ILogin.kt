package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.login._new.LoginRes
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ILogin {

    @FormUrlEncoded
    @POST(ConstantLinks.LOGIN_ENDPOINT)
    suspend fun login(
        @Field("user_name") username: String?,
        @Field("password") password: String?,
    ): LoginRes

}