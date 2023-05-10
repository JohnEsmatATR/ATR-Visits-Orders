package com.akhnaton.foodvisits.data.statusValue.db.saveVisit

import com.akhnaton.foodvisits.data.db.model.SaveVisitDB

sealed class SaveVisitState {
    object Idle : SaveVisitState()
    object Loading : SaveVisitState()
    data class SaveVisitList(val saveVisitList: List<SaveVisitDB>) : SaveVisitState()
    data class Error(val message: String) : SaveVisitState()
}
