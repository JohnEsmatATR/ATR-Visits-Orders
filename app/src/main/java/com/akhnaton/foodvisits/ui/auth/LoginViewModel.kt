package com.akhnaton.foodvisits.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.login.LoginIntent
import com.akhnaton.foodvisits.data.statusValue.login.LoginState
import com.akhnaton.foodvisits.domin.LoginRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val loginIntent = Channel<LoginIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)

    val state: StateFlow<LoginState> get() = _state

    init {
        makeLogin()
    }

    private fun makeLogin() {
        viewModelScope.launch {
            loginIntent.consumeAsFlow().collect {
                when (it) {
                    is LoginIntent.Login -> loginRepo(
                        it.version,
                        it.username,
                        it.password,
                    )
                }
            }
        }
    }

    private fun loginRepo(
        version: String,
        username: String,
        password: String,
    ) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            _state.value = try {
                LoginState.LogIn(LoginRepository().login(version, username, password))
            } catch (e: Exception) {
                LoginState.Error(e.message)
            }
        }
    }
}