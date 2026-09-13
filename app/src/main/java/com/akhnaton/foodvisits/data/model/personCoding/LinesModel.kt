package com.akhnaton.foodvisits.data.model.personCoding

data class LinesModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: List<LineItem>?
)

data class LineItem(
    val STORE_CODE: String,
    val LINE_CODE: String,
    val LINE_NAME: String,
    val JOB_FLAG: String
) {
    override fun toString(): String = LINE_NAME
}