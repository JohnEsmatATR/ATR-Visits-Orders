package com.akhnaton.foodvisits.shared

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.akhnaton.foodvisits.R
import com.google.android.material.button.MaterialButton

object DialogUtils {

    fun showResultDialog(
        context: Context,
        message: String,
        isSuccess: Boolean,
        seconds: Long? = null,
        showOkButton: Boolean = false,
        showYesNoButtons: Boolean = false,
        isDismissable: Boolean = false,
        isLocation: Boolean = false,
        isStartVisit: Boolean = false,
        okText: String? = "تم",
        onOk: (() -> Unit)? = null,
        onYes: (() -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onReport: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onStartVisit: (() -> Unit)? = null,
        onCancel2: (() -> Unit)? = null,
        description: String? = null,
        onAutoDismiss: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_result)
        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        dialog.setCanceledOnTouchOutside(isDismissable)
        dialog.setCancelable(isDismissable)

        val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)
        val tvDescription = dialog.findViewById<TextView>(R.id.tvDescription)
        val imgStatus = dialog.findViewById<ImageView>(R.id.imgStatus)
        val bgCircle = dialog.findViewById<View>(R.id.bgCircle)
        val btnOk = dialog.findViewById<TextView>(R.id.btnOk)
        val btnYes = dialog.findViewById<TextView>(R.id.btnYes)
        val btnNo = dialog.findViewById<TextView>(R.id.btnNo)
        val llLocation = dialog.findViewById<LinearLayout>(R.id.llLocation)
        val btnReport = dialog.findViewById<MaterialButton>(R.id.btnReport)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val llStartVisit = dialog.findViewById<LinearLayout>(R.id.llStartVisit)
        val btnStartVisit = dialog.findViewById<MaterialButton>(R.id.btnStartVisit)
        val btnCancel2 = dialog.findViewById<MaterialButton>(R.id.btnCancel2)

        tvMessage.text = message

        if (isSuccess) {
            imgStatus.setImageResource(R.drawable.ic_right)
//            bgCircle.setBackgroundResource(
//                R.drawable.circle_success_bg
//            )
        } else {
            imgStatus.setImageResource(R.drawable.ic_wrong)
//            bgCircle.setBackgroundResource(
//                R.drawable.circle_error_bg
//            )
        }

        btnOk.visibility = View.GONE
        btnYes.visibility = View.GONE
        btnNo.visibility = View.GONE

        if (isLocation) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = description
            llLocation.visibility = View.VISIBLE
            btnReport.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
            btnOk.visibility = View.GONE
            btnYes.visibility = View.GONE
            btnNo.visibility = View.GONE
            btnReport.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
            btnReport.setOnClickListener {
                dialog.dismiss()
                onReport?.invoke()
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
                onCancel?.invoke()
            }
        }

        if (isStartVisit) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = description
            llStartVisit.visibility = View.VISIBLE
            btnStartVisit.visibility = View.VISIBLE
            btnCancel2.visibility = View.VISIBLE
            btnOk.visibility = View.GONE
            btnYes.visibility = View.GONE
            btnNo.visibility = View.GONE
            btnStartVisit.visibility = View.VISIBLE
            btnCancel2.visibility = View.VISIBLE
            btnStartVisit.setOnClickListener {
                dialog.dismiss()
                onStartVisit?.invoke()
            }
            btnCancel2.setOnClickListener {
                dialog.dismiss()
                onCancel?.invoke()
            }
        }

        if (description != null) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = description
        }

        if (showOkButton) {

            btnOk.visibility = View.VISIBLE
            btnOk.text = okText
            btnOk.setOnClickListener {
                dialog.dismiss()
                onOk?.invoke()
            }
        }

        if (showYesNoButtons) {

            btnYes.visibility = View.VISIBLE
            btnNo.visibility = View.VISIBLE

            btnYes.setOnClickListener {
                dialog.dismiss()
                onYes?.invoke()
            }

            btnNo.setOnClickListener {
                dialog.dismiss()
                onNo?.invoke()
            }
        }

        if (seconds != null) {

            Handler(Looper.getMainLooper()).postDelayed({

                if (dialog.isShowing) {
                    dialog.dismiss()
                    onAutoDismiss?.invoke()
                }

            }, seconds * 1000)

        }

        dialog.show()
    }
}