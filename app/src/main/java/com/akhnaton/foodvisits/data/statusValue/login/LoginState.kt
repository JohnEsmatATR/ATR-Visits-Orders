package com.akhnaton.foodvisits.data.statusValue.login

import com.akhnaton.foodvisits.data.model.login._new.LoginRes

sealed class LoginState {

    object Idle : LoginState()
    object Loading : LoginState()
    data class LogIn(val login: LoginRes) : LoginState()
    data class Error(val error: String?) : LoginState()
}