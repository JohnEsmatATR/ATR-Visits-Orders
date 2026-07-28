package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ITickets
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class TicketsRepository {

    private val retrofit = RetrofitClient.getInstance(ITickets::class.java)

    suspend fun sendTickets(ticketsFun: String, messages: String, userId: String) =
        retrofit.sendTicket(ticketsFun, userId, messages)

    suspend fun getAllUsers() = retrofit.getAllUsers()

    suspend fun createTicket(
        phone: RequestBody,
        cc: RequestBody,
        subtitle: RequestBody,
        description: RequestBody,
        files: List<MultipartBody.Part>?
    ) =
        retrofit.createTicket(phone, cc, subtitle, description, files)
}