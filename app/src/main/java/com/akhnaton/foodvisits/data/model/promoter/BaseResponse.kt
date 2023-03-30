package com.akhnaton.foodvisits.data.model.promoter

data class BaseResponse<T>(
    var status: Int? = null,
    var data: List<T>? = null
)