package com.akhnaton.foodvisits.shared

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import com.google.android.material.R

class SpinnerHelper {

    fun setNormalSpinnerAdapter(
        spinner: Spinner?, list: MutableList<String>,
        context: Context
    ) {
        val mAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            list
        )
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        if (spinner != null) {
            spinner.adapter = mAdapter
        }
    }

    fun setAutoCompleteSpinnerAdapter(
        spinner: AutoCompleteTextView?, list: MutableList<String>,
        context: Context
    ) {
        val mAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            list
        )
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.setAdapter(mAdapter)
    }


    fun getVisitTypeFromSpinner(
        spinner: Spinner,
    ): String {

        return if (spinner.selectedItem.equals("طلبية")) {
            "A"
        } else {
            "C"
        }

    }

}