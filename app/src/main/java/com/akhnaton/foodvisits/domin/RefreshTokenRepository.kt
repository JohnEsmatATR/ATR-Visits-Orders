package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IBase
import com.akhnaton.foodvisits.data.interfaces.apis.ILogin
import com.akhnaton.foodvisits.shared.RetrofitClient

class RefreshTokenRepository {
    private val retrofit = RetrofitClient.getInstance(IBase::class.java)

    suspend fun refreshToken(
        userId: String,
        token: String,
    ) = retrofit.refreshToken(userId, token)
}