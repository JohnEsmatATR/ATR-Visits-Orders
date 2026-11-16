package com.akhnaton.foodvisits.ui.home.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.chartInfo.ChartInfoIntent
import com.akhnaton.foodvisits.data.statusValue.chartInfo.ChartInfoStatus
import com.akhnaton.foodvisits.domin.ChartInfoRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class ChartInfoViewModel : ViewModel() {

    val chartInfoIntent = Channel<ChartInfoIntent>(Channel.UNLIMITED)
    private val _status = MutableStateFlow<ChartInfoStatus?>(null)
    val status: StateFlow<ChartInfoStatus?> = _status

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            chartInfoIntent.consumeAsFlow().collect {
                when (it) {
                    is ChartInfoIntent.GetChartInfo -> getChartInfo()
                    is ChartInfoIntent.RefreshToken -> refreshToken(it.userId, it.token)
                }
            }
        }
    }

    private fun getChartInfo() {
        viewModelScope.launch {
            _status.value = ChartInfoStatus.Loading
            _status.value = try {
                ChartInfoStatus.GetChartInfo(ChartInfoRepository().getChartInfo())
            } catch (e: Exception) {
                ChartInfoStatus.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        viewModelScope.launch {
            _status.value = ChartInfoStatus.Loading
            _status.value = try {
                ChartInfoStatus.RefreshToken(PhoneVisitsRepository().refreshToken(userId, token))
            } catch (e: Exception) {
                ChartInfoStatus.Error(e.message)
            }
        }
    }

}