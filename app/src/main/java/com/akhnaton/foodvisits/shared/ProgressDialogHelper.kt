package com.akhnaton.foodvisits.shared

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.R
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

    fun showAlertProgress(
        context: Context,
        message: String
    ): AlertDialog {

        val view =
            LayoutInflater.from(context)
                .inflate(
                    R.layout.dialog_loading,
                    null
                )

        view.findViewById<TextView>(
            R.id.tvMessage
        ).text = message

        val dialog =
            AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        dialog.show()

        return dialog
    }


    fun orderLimitAlert(context: Context, message: String) {
        val limitAlert = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
        limitAlert.setTitleText("Order Limit!..")
        limitAlert.setContentText("Check for quantity, $message")
        limitAlert.setCancelable(false)
        limitAlert.show()
    }

    fun gpsAlert(context: Context) {
        val limitAlert = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
        limitAlert.setTitleText("GPS Error")
        limitAlert.setContentText("يوجد خطأ في تسجيل الزيارة، برجاء اغلاق وضع المطور أولاً")
        limitAlert.setCancelable(false)
        limitAlert.setConfirmClickListener {
            try {

                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {

                val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
            }
            limitAlert.dismiss()
        }
        limitAlert.show()
    }


}