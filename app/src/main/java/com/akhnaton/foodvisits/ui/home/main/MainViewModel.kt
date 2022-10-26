package com.akhnaton.foodvisits.ui.home.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.chart.ChartIntent
import com.akhnaton.foodvisits.data.statusValue.chart.ChartStatus
import com.akhnaton.foodvisits.domin.MainFragmentRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val chartIntent = Channel<ChartIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<ChartStatus>(ChartStatus.Idle)

    val state: StateFlow<ChartStatus> get() = _state

    init {
        getChartData()
    }

    private fun getChartData() {
        viewModelScope.launch {
            chartIntent.consumeAsFlow().collect {
                when (it) {
                    is ChartIntent.Chart -> getData(
                        it.app_version,
                        it.api_token,
                    )
                }
            }
        }
    }


    private fun getData(appVersion: String, apiToken: String) {
        viewModelScope.launch {
            _state.value = ChartStatus.Loading
            _state.value = try {
                ChartStatus.ChartData(MainFragmentRepository().getChart(appVersion, apiToken))
            } catch (e: Exception) {
                ChartStatus.Error(e.message)
            }
        }
    }
}