package com.akhnaton.foodvisits.ui.home.addCustomer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.addCustomer.GetGovernoratesIntent
import com.akhnaton.foodvisits.data.statusValue.addCustomer.GetGovernoratesState
import com.akhnaton.foodvisits.domin.AddCustomerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class GetGovernoratesViewModel : ViewModel() {

    val governoratesIntent = Channel<GetGovernoratesIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<GetGovernoratesState>(GetGovernoratesState.Idle)
    val state: StateFlow<GetGovernoratesState> get() = _state

    init {
        handleIntents()
    }

    private fun handleIntents() {
        viewModelScope.launch {
            governoratesIntent.consumeAsFlow().collect { intent ->
                when (intent) {
                    is GetGovernoratesIntent.GetGovernorate -> {
                        fetchGovernorates(intent.version, intent.token)
                    }
                    is GetGovernoratesIntent.GetCity -> {
                        fetchAreas(intent.version, intent.token, intent.governorateId)
                    }
                }
            }
        }
    }

    private fun fetchGovernorates(version: String, token: String) {
        viewModelScope.launch {
            _state.value = GetGovernoratesState.Loading
            _state.value = try {
                GetGovernoratesState.GovernoratesSuccess(
                    AddCustomerRepository().getGovernorates(version, token)
                )
            } catch (e: Exception) {
                GetGovernoratesState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchAreas(version: String, token: String, governorateId: String) {
        viewModelScope.launch {
            _state.value = GetGovernoratesState.Loading
            _state.value = try {
                GetGovernoratesState.AreasSuccess(
                    AddCustomerRepository().getAreas(version, token, governorateId)
                )
            } catch (e: Exception) {
                GetGovernoratesState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
