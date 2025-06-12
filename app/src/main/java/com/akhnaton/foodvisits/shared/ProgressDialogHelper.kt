package com.akhnaton.foodvisits.shared

import android.app.ProgressDialog
import android.content.Context
import cn.pedant.SweetAlert.SweetAlertDialog
import com.devhoony.lottieproegressdialog.LottieProgressDialog

class ProgressDialogHelper {

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

    fun errorMessage(context: Context, message: String) {
        SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE).setTitleText("Error")
            .setContentText(message).show()
    }



    fun showAlertProgress(context: Context,message :String): ProgressDialog {
        val progressBar = ProgressDialog(context)
        progressBar.setCancelable(false)
        progressBar.setMessage(message)
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        return progressBar
    }

    fun orderLimitAlert(context: Context, message :String) {
        val limitAlert = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
        limitAlert.setTitleText("Order Limit!..")
        limitAlert.setContentText("Check for quantity, $message")
        limitAlert.setCancelable(false)
        limitAlert.show()
    }

    fun gpsAlert(context: Context) {
        val limitAlert = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
        limitAlert.setTitleText("GPS Error").contentText = " يوجد خطآ في تسجيل الزياره"
        limitAlert.setCancelable(false)
        limitAlert.show()
    }

}