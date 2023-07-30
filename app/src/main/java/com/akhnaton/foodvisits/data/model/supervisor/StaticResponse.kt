package com.akhnaton.foodvisits.data.model.supervisor


class StaticResponse(
    var status: Int,
    var data: List<objectA>,
    var message: String,
)

data class objectA(
    var a:String
)