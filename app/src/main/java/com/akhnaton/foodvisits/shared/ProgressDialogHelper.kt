package com.akhnaton.foodvisits.shared

import android.app.AlertDialog
import android.content.Context
import android.widget.ProgressBar
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



    fun showAlertProgress(context: Context, message: String): AlertDialog {
        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(message)
            .setView(progressBar)
            .setCancelable(false)
            .create()

        dialog.show()
        return dialog
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
        limitAlert.setTitleText("GPS Error").contentText = "  يوجد خطآ في تسجيل الزياره برجاء اغلاق وضع المطور من الاعدادات "
        limitAlert.setCancelable(false)
        limitAlert.show()
    }

}