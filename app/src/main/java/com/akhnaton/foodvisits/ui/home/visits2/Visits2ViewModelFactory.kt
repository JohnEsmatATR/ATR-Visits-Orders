package com.akhnaton.foodvisits.ui.home.visits2

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModel

class Visits2ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Visits2ViewModel::class.java)) {
            return Visits2ViewModel(context) as T
        }
        throw IllegalArgumentException("Visits ViewModel class")
    }
}