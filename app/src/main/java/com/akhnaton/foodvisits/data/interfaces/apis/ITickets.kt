package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.CreateTicketRes
import com.akhnaton.foodvisits.data.model.getAllUsers.GetAllUsersRes
import com.akhnaton.foodvisits.data.model.tickets.Tickets
import com.akhnaton.foodvisits.shared.ConstantLinks.CREATE_TICKET
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ALL_USERS
import com.akhnaton.foodvisits.shared.ConstantLinks.TICKET_SYSTEM
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ITickets {

    @FormUrlEncoded
    @POST(TICKET_SYSTEM)
    suspend fun sendTicket(
        @Field("app_version") ticketSystem: String,
        @Field("api_token") userId: String?,
        @Field("message") messages: String?
    ): Tickets

    @GET(GET_ALL_USERS)
    suspend fun getAllUsers(): GetAllUsersRes

    @Multipart
    @POST(CREATE_TICKET)
    suspend fun createTicket(
        @Part("phone") phone: RequestBody,
        @Part("cc") cc: RequestBody,
        @Part("subtitle") subtitle: RequestBody,
        @Part("description") description: RequestBody,
        @Part files: List<MultipartBody.Part>?
    ): CreateTicketRes
}