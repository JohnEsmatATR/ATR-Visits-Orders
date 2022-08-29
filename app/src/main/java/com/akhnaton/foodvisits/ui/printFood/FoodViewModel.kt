package com.akhnaton.foodvisits.ui.printFood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.food.FoodIntent
import com.akhnaton.foodvisits.data.statusValue.food.FoodStatus
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.domin.FoodRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {

    val foodIntent = Channel<FoodIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<FoodStatus>(FoodStatus.Idle)

    val status: StateFlow<FoodStatus> get() = _status


    init {
        getFoodOrders()
    }

    private fun getFoodOrders() {

        viewModelScope.launch {
            foodIntent.consumeAsFlow().collect {
                when (it) {
                    is FoodIntent.Food -> fetchFoodOrders(it.version, it.token)
                    is FoodIntent.OrderDetails -> fetchFoodOrderDetails(
                        it.version,
                        it.token,
                        it.orderNumber
                    )
                }
            }
        }
    }


    private fun fetchFoodOrders(version: String, token: String) {
        viewModelScope.launch {
            _status.value = FoodStatus.Idle
            _status.value = try {
                FoodStatus.FoodOrders(FoodRepository().getFoodOrders(version, token))
            } catch (e: Exception) {
                FoodStatus.Error(e.message)
            }
        }
    }

    private fun fetchFoodOrderDetails(version: String, token: String, orderNumber: String) {
        viewModelScope.launch {
            _status.value = FoodStatus.Idle
            _status.value = try {
                FoodStatus.OrderDetails(
                    FoodRepository().getOrderDetails(
                        version,
                        token,
                        orderNumber
                    )
                )
            } catch (e: Exception) {
                FoodStatus.Error(e.message)
            }
        }
    }

}