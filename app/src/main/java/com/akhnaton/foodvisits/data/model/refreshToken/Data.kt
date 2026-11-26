package com.akhnaton.foodvisits.data.model.refreshToken

data class Data(
    val TOKEN: String,
    val ALLOWED_TO_MAKE_ORDER: Boolean,
    val ALLOWED_TO_MAKE_RETURN: Boolean,
    val ALLOWED_TO_MAKE_VISIT: Boolean,
)