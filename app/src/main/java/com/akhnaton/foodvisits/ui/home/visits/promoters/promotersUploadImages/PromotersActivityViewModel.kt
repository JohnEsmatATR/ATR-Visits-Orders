package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersUploadImages

import retrofit2.http.Part
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.domin.PromoterRepository
import okhttp3.RequestBody
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class PromotersActivityViewModel : ViewModel() {

    val promoterIntent = Channel<PromoterIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PromoterStatus>(PromoterStatus.Idle)

    val status: StateFlow<PromoterStatus> get() = _status

    init {
        uploadPhotos()
    }


    private fun uploadPhotos() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.UploadImages -> fetchUploadPhotos(
                        it.appVersion,
                        it.apiToken,
                        it.image,
                        it.created_by,
                        it.creation_date,
                        it.customer_code,
                        it.party_site_id,
                        it.user_type,
                        it.funNum
                    )

                    else -> {}
                }
            }
        }
    }

    private fun fetchUploadPhotos(
        appVersion: RequestBody?,
        apiToken: RequestBody?,
        image: Array<MultipartBody.Part?>,
        created_by: RequestBody?,
        creation_date: RequestBody?,
        customer_code: RequestBody?,
        party_site_id: RequestBody?,
        user_type: RequestBody?,
        funNum: RequestBody?,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.UploadImages(
                    PromoterRepository().uploadImages(
                        appVersion,
                        apiToken,
                        image,
                        created_by,
                        creation_date,
                        customer_code,
                        party_site_id,
                        user_type,
                        funNum
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }
}