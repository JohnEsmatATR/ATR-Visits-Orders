package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.shared.ConstantLinks
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrder
import com.akhnaton.foodvisits.data.model.supervisor.showOrder.SuperStatus
import com.akhnaton.foodvisits.data.model.supervisor.StaticResponse
import com.akhnaton.foodvisits.data.model.supervisor.creditDetails.CreditLimitDetails
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ISupervisor {

    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_ORDER_LIST)
    suspend fun getSuperOrders(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("super_id") superId: String?,
    ): SuperStatus?


    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_ORDER_REJECT)
    suspend fun rejectSuperOrder(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("order_number") orderNumber: String?,
    ): StaticResponse?


    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_ORDER_DETAILS)
    suspend fun getOrderDetailsSuper(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("order_number") orderNumber: String?,
        @Field("super_id") superId: String?,
        @Field("order_total_price") order_total_price: String?,
        @Field("customer_id") customer_id: String?,
    ): SuperOrder?


    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_CHECK_CREDIT_LIMIT)
    suspend fun checkCreditLimit(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("order_number") orderNumber: String?,
    ): StaticResponse?

    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_GET_CREDIT_LIMIT_DETAILS)
    suspend fun getCreditLimit(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("order_number") orderNumber: String?,
        @Field("customer_id") customer_id: String?,
        @Field("order_total_price") order_total_price: String?,
        ): CreditLimitDetails?

    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_SEND_CREDIT_LIMIT_DETAILS)
    suspend fun sendCreditLimitForm(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("customer_code") customer_code: String?,
        @Field("customer_name") customer_name: String?,
        @Field("customer_description") customer_description: String?,
        @Field("customer_branch") customer_branch: String?,
        @Field("branch") branch: String?,
        @Field("customer_method_payment") customer_method_payment: String?,
        @Field("customer_current_opening_status") customer_current_opening_status: String?,
        @Field("customer_commercial_register") customer_commercial_register: String?,
        @Field("customer_previous_credit_limit") customer_previous_credit_limit: String?,
        @Field("customer_quarter_plan") customer_quarter_plan: String?,
        @Field("sell_client_to_date") sell_client_to_date: String?,
        @Field("investigation_ratio") investigation_ratio: String?,
        @Field("customer_required_credit") customer_required_credit: String?,
        @Field("customer_number_branches") customer_number_branches: String?,
        @Field("customer_guarantee") customer_guarantee: String?,
        @Field("customer_current_limit") customer_current_limit: String?,
        @Field("customer_withdrwals_current_year") customer_withdrwals_current_year: String?,
        @Field("customer_withdrwals_last_year") customer_withdrwals_last_year: String?,
        @Field("customer_bounces_current_year_forward_transaction") customer_bounces_current_year_forward_transaction: String?,
        @Field("customer_overrun_money") customer_overrun_money: String?,
        @Field("customer_insurance") customer_insurance: String?,
        @Field("customer_entered_orders") customer_entered_orders: String?,
        @Field("customer_booked_orders") customer_booked_orders: String?,
        @Field("customer_check_remitted_amount") customer_check_remitted_amount: String?,
        @Field("customer_total_limit") customer_total_limit: String?,
        @Field("remaining_credit") remaining_credit: String?,
        @Field("customer_national_id") customer_national_id: String?,
        @Field("customer_security_cheques") customer_security_cheques: String?,
        @Field("order_number") order_number: String?,
    ): StaticResponse?


    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_CHECK_QOUTA)
    suspend fun checkQouta(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("order_number") orderNumber: String?,
        @Field("super_id") superId: String?,
    ): StaticResponse?


    @FormUrlEncoded
    @POST(ConstantLinks.SUPER_APPROVE_ORDER)
    suspend fun approveOrder(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("order_number") orderNumber: String?,
        @Field("super_id") superId: String?,
    ): StaticResponse?

}