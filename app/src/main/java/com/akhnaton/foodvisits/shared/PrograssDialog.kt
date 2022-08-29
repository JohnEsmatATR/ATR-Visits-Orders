package com.akhnaton.foodvisits.shared

import android.app.ProgressDialog
import android.content.Context
import cn.pedant.SweetAlert.SweetAlertDialog
import com.devhoony.lottieproegressdialog.LottieProgressDialog

class ProgressDialog {

    fun showProgress(context: Context): LottieProgressDialog {
        return LottieProgressDialog(
            context = context,
            isCancel = true,
            dialogWidth = null,
            dialogHeight = null,
            animationViewWidth = null,
            animationViewHeight = null,
            fileName = LottieProgressDialog.SAMPLE_2,
            title = null,
            titleVisible = null
        )
    }


    fun showAlertProgress(context: Context): ProgressDialog {
        val progressBar = ProgressDialog(context)
        progressBar.setCancelable(true)
        progressBar.setMessage("Order Sending..")
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        return progressBar
    }

    fun orderLimitAlert(context: Context, message :String) {
        val limitAlert = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
        limitAlert.setTitleText("Order Limit!..").contentText = message
        limitAlert.setCancelable(false)
        limitAlert.show()
    }

}