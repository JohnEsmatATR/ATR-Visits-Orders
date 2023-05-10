package com.akhnaton.foodvisits.data.statusValue.db.saveVisit

import com.akhnaton.foodvisits.data.db.model.SaveVisitDB

sealed class SaveVisitIntent {
    object GetSaveVisitList : SaveVisitIntent()
    data class AddSaveVisit(val saveVisit: SaveVisitDB) : SaveVisitIntent()
    data class DeleteSaveVisit(val saveVisit: SaveVisitDB) : SaveVisitIntent()
}