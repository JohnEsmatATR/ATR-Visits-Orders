package com.akhnaton.foodvisits.ui.home.visitPlan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.akhnaton.foodvisits.data.dummy.DummyVisitsProvider
import com.akhnaton.foodvisits.data.model.Visit

class VisitPlanViewModel : ViewModel() {

    private val allVisits: List<Visit> = DummyVisitsProvider.generate()

    val daysWithVisits: Set<String> by lazy { allVisits.map { it.date }.toSet() }

    private val _visitsForSelectedDate = MutableLiveData<List<Visit>>()
    val visitsForSelectedDate: LiveData<List<Visit>> = _visitsForSelectedDate

    fun loadVisitsFor(dateKey: String) {
        _visitsForSelectedDate.value = allVisits.filter { it.date == dateKey }
    }
}