package com.akhnaton.foodvisits.ui.home.visits

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VisitsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitsViewModel::class.java)) {
            return VisitsViewModel(context) as T
        }
        throw IllegalArgumentException("Visits ViewModel class")
    }
}