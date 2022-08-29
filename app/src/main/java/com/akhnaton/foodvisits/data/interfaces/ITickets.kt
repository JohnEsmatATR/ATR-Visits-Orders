package com.akhnaton.foodvisits.data.interfaces

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ITickets {

    @FormUrlEncoded
    @POST("api/order_api.php")
    suspend fun sendTicket(
        @Field("Ticket_System") ticketSystem: Int,
        @Field("messages") messages: String?,
        @Field("user_id") userId: String?
    ): String
}