package com.akhnaton.foodvisits.data.statusValue.login

sealed class LoginIntent {

    data class Login(
        val username: String,
        val password: String,
    ) : LoginIntent()

}