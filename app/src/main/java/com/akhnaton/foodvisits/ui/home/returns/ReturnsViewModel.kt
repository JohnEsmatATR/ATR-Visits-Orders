package com.akhnaton.foodvisits.ui.home.returns

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsIntent
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsStatus
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import com.akhnaton.foodvisits.domin.ReturnsRepository
import com.akhnaton.foodvisits.domin.Visits2Repository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class ReturnsViewModel : ViewModel() {

    val returnsIntent = Channel<ReturnsIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<ReturnsStatus>(ReturnsStatus.Idle)

    val status: StateFlow<ReturnsStatus> get() = _status

    init {
        getReturns()
    }

    private fun getReturns() {
        viewModelScope.launch {
            returnsIntent.consumeAsFlow().collect {
                when (it) {
                    is ReturnsIntent.GetPriceLists -> getPriceLists(
                        it.partySiteId,
                        it.orderType,
                    )

                    else -> {}
                }
            }
        }
    }

    private fun getPriceLists(partySiteId: String, orderType: String) {
        Log.d("WHAT", "getPriceListsVIEWMODEL")
        viewModelScope.launch {
            _status.value = ReturnsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "getPriceListsVIEWMODEL1")
                ReturnsStatus.GetPriceLists(
                    ReturnsRepository().getPriceLists(partySiteId, orderType)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getPriceListsVIEWMODEL2 ${e.message}")
                ReturnsStatus.Error(e.message)
            }
        }
    }

    private fun startReturnData(orderId: String, priceListId: String) {
        Log.d("WHAT", "startReturnDataVIEWMODEL")
        viewModelScope.launch {
            _status.value = ReturnsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "startReturnDataVIEWMODEL1")
                ReturnsStatus.StartReturnData(
                    ReturnsRepository().startReturnData(orderId, priceListId)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "startReturnDataVIEWMODEL2 ${e.message}")
                ReturnsStatus.Error(e.message)
            }
        }
    }

}


