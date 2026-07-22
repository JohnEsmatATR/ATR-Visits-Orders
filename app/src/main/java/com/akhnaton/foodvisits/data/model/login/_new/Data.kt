package com.akhnaton.foodvisits.data.model.login._new

data class Data(
    val ALLOWED_TO_APPROVE_ORDER: Boolean,
    val ALLOWED_TO_APPROVE_VISIT: Boolean,
    val ALLOWED_TO_MAKE_ORDER: Boolean,
    val ALLOWED_TO_MAKE_RATE: Boolean,
    val ARABIC_USER_NAME: String,
    val ENGLISH_USER_NAME: String,
    val TOKEN: String,
    val USER_CATEGORY: String,
    val USER_EMAIL: String,
    val USER_ID: String,
    val USER_NAME: String
)