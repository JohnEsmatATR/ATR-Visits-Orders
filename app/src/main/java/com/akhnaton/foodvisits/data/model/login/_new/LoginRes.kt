package com.akhnaton.foodvisits.data.model.login._new

data class LoginRes(
    val `data`: List<Data>,
    val debug: List<Debug>,
    val last_query_finished: Int,
    val message: String,
    val status: Int,
    val type: String
)