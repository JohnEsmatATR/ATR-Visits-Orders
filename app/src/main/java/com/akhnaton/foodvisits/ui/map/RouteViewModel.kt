package com.akhnaton.foodvisits.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.route.RouteIntent
import com.akhnaton.foodvisits.data.statusValue.route.RouteState
import com.akhnaton.foodvisits.domin.location.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class RouteViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RouteState>(RouteState.Idle)
    val state: StateFlow<RouteState> = _state

    fun onIntent(intent: RouteIntent) {
        when (intent) {
            is RouteIntent.FetchRoute -> fetchRoute(intent.waypoints)
        }
    }

    private fun fetchRoute(waypoints: List<GeoPoint>) {
        viewModelScope.launch {
            _state.value = RouteState.Loading

            val result = repository.fetchRoute(waypoints)
            _state.value = result.fold(
                onSuccess = { RouteState.Success(it) },
                onFailure = { RouteState.Error(it.message ?: "Unknown Error") }
            )
        }
    }
}