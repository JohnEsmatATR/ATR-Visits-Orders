package com.akhnaton.foodvisits.shared

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.akhnaton.foodvisits.R

object DialogUtils {

    fun showResultDialog(
        context: Context,
        message: String,
        isSuccess: Boolean,
        seconds: Long? = null,
        showOkButton: Boolean = false,
        showYesNoButtons: Boolean = false,
        onOk: (() -> Unit)? = null,
        onYes: (() -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onAutoDismiss: (() -> Unit)? = null
    ) {

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.dialog_result)

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        val tvMessage =
            dialog.findViewById<TextView>(R.id.tvMessage)

        val imgStatus =
            dialog.findViewById<ImageView>(R.id.imgStatus)

        val bgCircle =
            dialog.findViewById<View>(R.id.bgCircle)

        val btnOk =
            dialog.findViewById<TextView>(R.id.btnOk)

        val btnYes =
            dialog.findViewById<TextView>(R.id.btnYes)

        val btnNo =
            dialog.findViewById<TextView>(R.id.btnNo)

        tvMessage.text = message

        if (isSuccess) {
            imgStatus.setImageResource(R.drawable.ic_right)
            bgCircle.setBackgroundResource(
                R.drawable.circle_success_bg
            )
        } else {
            imgStatus.setImageResource(R.drawable.ic_wrong)
            bgCircle.setBackgroundResource(
                R.drawable.circle_error_bg
            )
        }

        btnOk.visibility = View.GONE
        btnYes.visibility = View.GONE
        btnNo.visibility = View.GONE

        if (showOkButton) {

            btnOk.visibility = View.VISIBLE

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