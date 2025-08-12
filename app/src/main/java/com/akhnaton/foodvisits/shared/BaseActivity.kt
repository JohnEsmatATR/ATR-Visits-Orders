package com.akhnaton.foodvisits.shared

import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper.Companion.context
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


open class BaseActivity : AppCompatActivity() {
    var dp = 0f

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR)
        super.onCreate(savedInstanceState, persistentState)
        dp = resources.displayMetrics.density

    }

    override fun onResume() {
        super.onResume()
    }

    fun showToastSnack(word: String?, flag: Boolean) {
        try {
            val layout = LayoutInflater.from(this).inflate(R.layout.snack_bar_layout, null, false)
            layout.setBackgroundColor(
                if (flag) ContextCompat.getColor(context, R.color.red) else ContextCompat.getColor(context,  R.color.green
                )
            )
            val image = layout.findViewById<ImageView>(R.id.image)
            image.setImageResource(if (flag) R.drawable.ic_error else R.drawable.ic_success)
            val text = layout.findViewById<TextView>(R.id.text)
            text.text = word
            text.setTextColor(ContextCompat.getColor(context, R.color.white))
            val parentLayout = findViewById<View>(android.R.id.content)
            val snackbar = Snackbar.make(parentLayout, "", BaseTransientBottomBar.LENGTH_SHORT)
            (snackbar.view as ViewGroup).removeAllViews()
            (snackbar.view as ViewGroup).addView(layout)
            val params = snackbar.view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbar.view.setBackgroundColor(
                if (flag)ContextCompat.getColor(context, R.color.red)
                else ContextCompat.getColor(context, R.color.green)
            )
            snackbar.setBackgroundTint(
                if (flag) ContextCompat.getColor(context, R.color.red)
                else ContextCompat.getColor(context, R.color.green)
            )

            snackbar.view.layoutParams = params
            snackbar.view.setPadding(
                (16 * dp).toInt(),
                (10 * dp).toInt(),
                (16 * dp).toInt(),
                (10 * dp).toInt()
            )
            snackbar.show()
        } catch (e: Exception) {

        }
    }

    fun showToast(view: View, str: String) {
        val snack: Snackbar = Snackbar.make(view, str, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundResource(R.color.green)
        snack.show()
    }

    fun showDialog(title: String, message: String, isCancelable: Boolean): AlertDialog.Builder {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@BaseActivity)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(isCancelable)
        return builder
    }

    fun showProgressDialog(view: View) {
        view.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun hideProgressDialog(view: View) {
        view.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


}