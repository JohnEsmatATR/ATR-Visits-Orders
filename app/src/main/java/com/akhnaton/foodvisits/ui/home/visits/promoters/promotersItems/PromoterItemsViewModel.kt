package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersItems

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.domin.PromoterRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class PromoterItemsViewModel : ViewModel() {

    val promoterIntent = Channel<PromoterIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PromoterStatus>(PromoterStatus.Idle)

    val status: StateFlow<PromoterStatus> get() = _status

    init {
        getItems()
    }

    private fun getItems() {
        viewModelScope.launch {

            promoterIntent.consumeAsFlow().collect {
                Log.d("PromoterItemActivity", "getItems: Viewmodel")

                when (it) {
                    is PromoterIntent.GetItems -> fetchGetItems(
                        it.items,
                        it.createdBy,
                        it.customerCode,
                        it.partySiteId
                    )
                }
            }
        }
    }

    private fun fetchGetItems(
        items: String,
        createdBy: String,
        customerCode: String,
        partySiteId: String,
    ) {
        viewModelScope.launch {
            Log.d("PromoterItemActivity", "fetchGetItems: Viewmodel")

            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.GetItems(
                    PromoterRepository().getItems(
                        items,
                        createdBy,
                        customerCode,
                        partySiteId
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }
}