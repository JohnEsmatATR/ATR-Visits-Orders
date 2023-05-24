package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IOrder
import com.akhnaton.foodvisits.shared.RetrofitClient
import com.google.gson.JsonElement
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.ResponseBody
import org.json.JSONObject

class OrderRepository {

    private val retrofit = RetrofitClient.getInstance(IOrder::class.java)

    suspend fun generateOrderNumber(
        appVersion: String,
        apiToken: String,
        customerPartySiteId: String,
        orderType: String,
        customerType: String,
        paymentTermId: String,
        visitId: String
    ) =
        retrofit.generateOrderNumber(
            appVersion,
            apiToken,
            customerPartySiteId,
            orderType,
            customerType, paymentTermId, visitId
        )

    suspend fun getCategories(
        appVersion: String,
        apiToken: String,
        orderType: String,
        customer_type: String,
    ) = retrofit.getCategories(appVersion = appVersion, apiToken = apiToken, orderType = orderType, customer_type = customer_type)

    suspend fun getProducts(
        appVersion: String,
        apiToken: String,
        orderType: String,
        subCategory: String,
        customerType: Int,
        customerCode: Int,
        customerPartySiteId: Int,
    ) = retrofit.getProduct(
        appVersion = appVersion,
        apiToken = apiToken,
        orderType = orderType,
        subCategory,
        customerType,
        customerCode,
        customerPartySiteId
    )

    suspend fun getOrderLimit(appVersion: String) = retrofit.getOrderLimit(appVersion)

    suspend fun sendOrder(request: JsonElement) = retrofit.sendOrder(request)
}