package com.akhnaton.foodvisits.data.model

data class BaseModel<T>(
    val message: String = "",
    val status: Int = 0,
    var data: T?,
)