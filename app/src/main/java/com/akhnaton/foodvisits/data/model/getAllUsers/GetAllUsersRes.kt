package com.akhnaton.foodvisits.data.model.getAllUsers

data class GetAllUsersRes(
    val `data`: List<Data>,
    val message: String,
    val status: Int,
    val type: String
)