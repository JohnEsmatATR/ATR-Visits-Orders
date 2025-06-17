package com.akhnaton.foodvisits.data.statusValue.login

sealed class LoginIntent {

    data class Login(
        val version: String,
        val username: String,
        val password: String,
        val firebaseToken:String
    ) : LoginIntent()

}