package com.akhnaton.foodvisits.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingIntent
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingStatus
import com.akhnaton.foodvisits.domin.AppSettingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {

    val mainIntent = Channel<AppSettingIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<AppSettingStatus>(AppSettingStatus.Idle)

    val state: StateFlow<AppSettingStatus> get() = _state


    init {
        getAppSetting()
    }

    private fun getAppSetting() {
        viewModelScope.launch {
            mainIntent.consumeAsFlow().collect {
                when (it) {
                    is AppSettingIntent.GetAppSetting -> fetchAppSetting(it.app_version)
                }
            }
        }
    }

    private fun fetchAppSetting(appVersion: String) {
        viewModelScope.launch {
            _state.value = AppSettingStatus.Loading
            _state.value = try {
                AppSettingStatus.GetAppSetting(
                    AppSettingRepository().getAppSetting(
                        appVersion = appVersion,
                    )
                )
            } catch (e: Exception) {
                AppSettingStatus.Error(e.message)
            }
        }
    }
}