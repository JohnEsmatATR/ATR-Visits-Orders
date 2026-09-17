package com.akhnaton.foodvisits.ui.home.personCoding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.personCoding.AddCustomerModel
import com.akhnaton.foodvisits.data.statusValue.personCoding.PersonIntent
import com.akhnaton.foodvisits.data.statusValue.personCoding.PersonStatus
import com.akhnaton.foodvisits.domin.PersonCodingRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class PersonCodingViewModel : ViewModel() {

    val customerIntent = Channel<PersonIntent>(Channel.UNLIMITED)
    private val _status = MutableStateFlow<PersonStatus?>(null)
    val status: StateFlow<PersonStatus?> = _status

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            customerIntent.consumeAsFlow().collect {
                when (it) {
                    is PersonIntent.GetSalesAndCustomerTypes -> getSalesAndCustomerTypes()
                    is PersonIntent.GetLines -> getLines(it.saleType, it.customerType)
                    is PersonIntent.GetMainCustomersLine -> getMainCustomersLine(
                        it.lineId, it.orderType, it.customerType
                    )
                    is PersonIntent.RefreshToken -> refreshToken(it.userId, it.token)
                    is PersonIntent.GetUserAreas -> getUserAreas()
                    is PersonIntent.GetAreasByGovernorate -> getAreasByGovernorate(it.governorateId)
                    is PersonIntent.AddCustomer -> addCustomer(it.fields, it.frontImage, it.backImage)
                }
            }
        }
    }

    private fun getSalesAndCustomerTypes() {
        viewModelScope.launch {
            _status.value = PersonStatus.Loading
            _status.value = try {
                PersonStatus.GetSalesAndCustomerTypes(
                    PersonCodingRepository().getSalesAndCustomerTypes()
                )
            } catch (e: Exception) {
                PersonStatus.Error(e.message)
            }
        }
    }

    private fun getLines(saleType: String, customerType: String) {
        viewModelScope.launch {
            _status.value = PersonStatus.Loading
            _status.value = try {
                PersonStatus.GetLines(
                    PersonCodingRepository().getLines(saleType, customerType)
                )
            } catch (e: Exception) {
                PersonStatus.Error(e.message)
            }
        }
    }

    private fun getMainCustomersLine(lineId: String, orderType: String, customerType: String) {
        viewModelScope.launch {
            _status.value = PersonStatus.Loading
            _status.value = try {
                PersonStatus.GetMainCustomersLine(
                    PersonCodingRepository().getMainCustomersLine(lineId, orderType, customerType)
                )
            } catch (e: Exception) {
                PersonStatus.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        viewModelScope.launch {
            _status.value = PersonStatus.Loading
            _status.value = try {
                PersonStatus.RefreshToken(
                    PhoneVisitsRepository().refreshToken(userId, token)
                )
            } catch (e: Exception) {
                PersonStatus.Error(e.message)
            }
        }
    }
    private fun getUserAreas() {
        viewModelScope.launch {
            _status.value = PersonStatus.Loading
            _status.value = try {
                PersonStatus.GetUserAreas(
                    PersonCodingRepository().getUserAreas()
                )
            } catch (e: Exception) {
                PersonStatus.Error(e.message)
            }
        }
    }
    private fun getAreasByGovernorate(governorateId: String) {
        viewModelScope.launch {
            _status.value = PersonStatus.Loading
            _status.value = try {
                PersonStatus.GetAreasByGovernorate(
                    PersonCodingRepository().getAreasByGovernorate(governorateId)
                )
            } catch (e: Exception) {
                PersonStatus.Error(e.message)
            }
        }
    }
    private fun addCustomer(
        fields: Map<String, RequestBody>,
        frontImage: MultipartBody.Part?,
        backImage: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _status.value = PersonStatus.Loading
            _status.value = try {
                PersonStatus.AddCustomer(
                    PersonCodingRepository().addCustomer(fields, frontImage, backImage)
                )
            } catch (e: HttpException) {
                val backendMessage = try {
                    val errorBodyString = e.response()?.errorBody()?.string()
                    if (!errorBodyString.isNullOrBlank()) {
                        com.google.gson.Gson().fromJson(
                            errorBodyString,
                            AddCustomerModel::class.java
                        ).message
                    } else {
                        null
                    }
                } catch (parseError: Exception) {
                    null
                }
                PersonStatus.Error(backendMessage ?: e.message())
            } catch (e: Exception) {
                PersonStatus.Error(e.message)
            }
        }
    }

}