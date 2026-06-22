package com.akhnaton.foodvisits.data.model.refreshToken

import com.google.gson.JsonElement

data class RefreshTokenRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)