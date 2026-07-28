package com.akhnaton.foodvisits.ui.home.tickets

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsIntent
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsStatus
import com.akhnaton.foodvisits.domin.TicketsRepository
import com.akhnaton.foodvisits.shared.uriToPart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class TicketsViewModel : ViewModel() {

    val ticketsIntent = Channel<TicketsIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<TicketsStatus>(TicketsStatus.Idle)

    val state: StateFlow<TicketsStatus> get() = _state


    init {
        sendTicketsData()
    }

    private fun sendTicketsData() {
        viewModelScope.launch {
            ticketsIntent.consumeAsFlow().collect {
                when (it) {
                    is TicketsIntent.Tickets -> sendTicket(
                        it.ticketsFun,
                        it.messages,
                        it.userId,
                    )

                    is TicketsIntent.GetAllUsers -> getAllUsers()

                    is TicketsIntent.CreateTicket -> createTicket(
                        it.phone,
                        it.cc,
                        it.subtitle,
                        it.description,
                        it.files,
                    )
                }
            }
        }
    }

    private fun sendTicket(ticketFun: String, message: String, userId: String) {
        viewModelScope.launch {
            _state.value = TicketsStatus.Loading
            _state.value = try {
                TicketsStatus.SendTickets(
                    TicketsRepository().sendTickets(
                        ticketFun,
                        message,
                        userId
                    )
                )
            } catch (e: Exception) {
                TicketsStatus.Error(e.message)
            }
        }
    }

    private fun getAllUsers() {
        viewModelScope.launch {
            _state.value = TicketsStatus.Loading
            _state.value = try {
                TicketsStatus.GetAllUsers(
                    TicketsRepository().getAllUsers()
                )
            } catch (e: Exception) {
                TicketsStatus.Error(e.message)
            }
        }
    }

    private fun createTicket(
        phone: RequestBody,
        cc: RequestBody,
        subtitle: RequestBody,
        description: RequestBody,
        files: List<MultipartBody.Part>?
    ) {
        viewModelScope.launch {
            _state.value = TicketsStatus.Loading
            _state.value = try {
                TicketsStatus.CreateTicket(
                    TicketsRepository().createTicket(
                        phone,
                        cc,
                        subtitle,
                        description,
                        files
                    )
                )
            } catch (e: Exception) {
                TicketsStatus.Error(e.message)
            }
        }
    }
}