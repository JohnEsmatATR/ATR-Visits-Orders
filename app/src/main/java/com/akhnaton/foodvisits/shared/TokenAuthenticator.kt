package com.akhnaton.foodvisits.shared

import com.akhnaton.foodvisits.data.model.refreshToken.Data
import com.akhnaton.foodvisits.domin.RefreshTokenRepository
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.runBlocking

class TokenAuthenticator : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        // منع اللوب
        if (responseCount(response) >= 2) {
            return null
        }

        try {

            val refreshResponse = runBlocking {
                RefreshTokenRepository()
                    .refreshToken(
                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                        SharedPreferencesHelper.getInstance().getUserToken()
                    )
            }

            if (refreshResponse.status == 200) {

                val data =
                    Gson().fromJson(
                        refreshResponse.data,
                        Data::class.java
                    )
                val newToken =
                    data.TOKEN
                        ?: return null

                SharedPreferencesHelper
                    .getInstance()
                    .saveUserToken(newToken)

                return response.request
                    .newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $newToken"
                    )
                    .build()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun responseCount(
        response: Response
    ): Int {

        var result = 1
        var current = response.priorResponse

        while (current != null) {
            result++
            current = current.priorResponse
        }

        return result
    }
}