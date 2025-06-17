package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.login.Login
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ILogin {

    @FormUrlEncoded
    @POST(ConstantLinks.LOGIN_PATH)
    suspend fun login(
        @Field("app_version") version: String?,
        @Field("user_name") username: String?,
        @Field("password") password: String?,
        @Field("firebase_token") firebaseToken :String?
    ): Login

}