package com.akhnaton.foodvisits.data.statusValue.customerCoding

import com.akhnaton.foodvisits.data.model.BaseModel
import com.akhnaton.foodvisits.data.model.coding.CodingAreaModel
import com.akhnaton.foodvisits.data.model.coding.CodingCategoryModel
import com.akhnaton.foodvisits.data.model.coding.CodingLineModel
import com.akhnaton.foodvisits.data.model.coding.CodingTypeModel

sealed class CustomerCodingState {

    object Idle : CustomerCodingState()
    object Loading : CustomerCodingState()
    data class GetTypes(val data: BaseModel<List<CodingTypeModel>>) : CustomerCodingState()
    data class GetLines(val data: BaseModel<List<CodingLineModel>>) : CustomerCodingState()
    data class GetCategories(val data: BaseModel<List<CodingCategoryModel>>) : CustomerCodingState()
    data class GetAreas(val data: BaseModel<List<CodingAreaModel>>) : CustomerCodingState()
    data class SendCustomer(val data: BaseModel<List<String>>) : CustomerCodingState()
    data class Error(val error: String?) : CustomerCodingState()
}