package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ILogin
import com.akhnaton.foodvisits.shared.RetrofitClient

class LoginRepository {
    private val retrofit = RetrofitClient.getInstance(ILogin::class.java)

    suspend fun login(
        username: String,
        password: String,
    ) = retrofit.login(username, password)
}