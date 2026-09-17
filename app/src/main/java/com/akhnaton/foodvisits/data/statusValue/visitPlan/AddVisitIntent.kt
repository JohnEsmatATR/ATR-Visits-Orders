package com.akhnaton.foodvisits.data.statusValue.visitPlan

sealed class AddVisitIntent {
    object GetSalesTypes : AddVisitIntent()
    data class GetLines(val saleType: String) : AddVisitIntent()
}