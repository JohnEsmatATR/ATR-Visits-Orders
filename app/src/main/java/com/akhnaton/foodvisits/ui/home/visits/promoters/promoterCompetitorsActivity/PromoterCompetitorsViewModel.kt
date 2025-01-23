package com.akhnaton.foodvisits.ui.home.visits.promoters.promoterCompetitorsActivity

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
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PromoterCompetitorsViewModel : ViewModel() {
    val promoterIntent = Channel<PromoterIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PromoterStatus>(PromoterStatus.Idle)

    val status: StateFlow<PromoterStatus> get() = _status

    init {
        getCompetitorList()
        sendCompetitors()
    }

    private fun sendCompetitors() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.SendCompetitors -> fetchSendCompetitors(
                        it.appVersion,
                        it.apiToken,
                        it.image,
                        it.created_by,
                        it.creation_date,
                        it.party_site_id,
                        it.customer_code,
                        it.product_id,
                        it.price,
                        it.price_after_disc,
                        it.product_name,
                        it.weight,
                        it.discount_rate,
                        it.prom_type,
                        it.prom_date,
                        it.user_type,
                        it.PromoterCompetitorCompress,
                        it.competitor_name,
                        it.type_name,
                    )

                    else -> {}
                }
            }
        }
    }
    private fun fetchSendCompetitors(
        appVersion: RequestBody,
        apiToken: RequestBody,
        image: Array<MultipartBody.Part?>,
        created_by: RequestBody,
        creation_date: RequestBody,
        party_site_id: RequestBody,
        customer_code: RequestBody,
        product_id: RequestBody,
        price: RequestBody,
        price_after_disc: RequestBody,
        product_name: RequestBody,
        weight: RequestBody,
        discount_rate: RequestBody,
        prom_type: RequestBody,
        prom_date: RequestBody,
        user_type: RequestBody,
        PromoterCompetitorCompress: RequestBody,
        competitor_name: RequestBody,
        type_name: RequestBody,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.SendCompetitors(
                    PromoterRepository().sendCompetitors(
                        appVersion,
                        apiToken,
                        image,
                        created_by,
                        creation_date,
                        party_site_id,
                        customer_code,
                        product_id,
                        price,
                        price_after_disc,
                        product_name,
                        weight,
                        discount_rate,
                        prom_type,
                        prom_date,
                        user_type,
                        PromoterCompetitorCompress,
                        competitor_name,
                        type_name,
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }


    private fun getCompetitorList() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.GetCompetitorList -> fetchGetCompetitorList(
                        it.appVersion,
                    )

                    else -> {}
                }
            }
        }
    }

    private fun fetchGetCompetitorList(
        appVersion: Double,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.GetCompetitorList(
                    PromoterRepository().getCompetitorList(
                        appVersion,
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }


}