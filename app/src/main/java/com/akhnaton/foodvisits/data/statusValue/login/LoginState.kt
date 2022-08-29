package com.akhnaton.foodvisits.data.statusValue.login

import com.akhnaton.foodvisits.data.model.login.Login

sealed class LoginState {

    object Idle : LoginState()
    object Loading : LoginState()
    data class LogIn(val login: Login) : LoginState()
    data class Error(val error: String?) : LoginState()
}