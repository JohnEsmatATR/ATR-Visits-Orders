package com.akhnaton.foodvisits.shared

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.akhnaton.foodvisits.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

open class BaseFragment : Fragment() {
    var dp = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dp = resources.displayMetrics.density

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

    }

    fun showToastSnack(word: String?, flag: Boolean) {
        try {
            val layout =
                LayoutInflater.from(context).inflate(R.layout.snack_bar_layout, null, false)
            layout.setBackgroundColor(
                if (flag) ContextCompat.getColor(requireContext(), R.color.red) else ContextCompat.getColor(requireContext(),  R.color.green
                )
            )
            val image = layout.findViewById<ImageView>(R.id.image)
            image.setImageResource(if (flag) R.drawable.ic_error else R.drawable.ic_success)
            val text = layout.findViewById<TextView>(R.id.text)
            text.text = word
            text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val snackbar =
                Snackbar.make(requireView().rootView, "", BaseTransientBottomBar.LENGTH_SHORT)
            (snackbar.view as ViewGroup).removeAllViews()
            (snackbar.view as ViewGroup).addView(layout)
            val params = snackbar.view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbar.view.setBackgroundColor(
                if (flag) ContextCompat.getColor(requireContext(), R.color.red)
                else ContextCompat.getColor(requireContext(), R.color.green)
            )
            snackbar.setBackgroundTint(
                if (flag) ContextCompat.getColor(requireContext(), R.color.red)
                else ContextCompat.getColor(requireContext(), R.color.green)
            )

            snackbar.view.layoutParams = params
            snackbar.view.setPadding(
                (16 * dp).toInt(),
                (20 * dp).toInt(),
                (16 * dp).toInt(),
                (10 * dp).toInt()
            )
            snackbar.show()
        } catch (e:Exception) {
            Log.d(Common.KeroDebug, "showToastSnack: ${e.message}")
        }
    }

    fun showToast(view: View, str: String) {
        val snack: Snackbar = Snackbar.make(view, str, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(Color.RED)
        snack.show()
    }

    fun showDialog(title: String, message: String, isCancelable: Boolean): AlertDialog.Builder {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(isCancelable)
        return builder
    }

    fun hideStatusBar() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun navigateTo(id: Int, bundle: Bundle?) {
        val navBuilder = NavOptions.Builder()
        Navigation.findNavController(requireView()).navigate(id, bundle, navBuilder.build())
    }

    fun navigateBack() {
        val flag = Navigation.findNavController(requireView()).popBackStack()
        if (!flag) {
            requireActivity().finish()
        }
    }


    fun validateIncreaseQuantity(qty: Int, max: Int): Boolean {
        return (qty < max)
    }

    fun validateDecreaseQuantity(qty: Int): Boolean {
        return (qty > 0)
    }


    fun showProgressDialog(view: View) {
        view.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun hideProgressDialog(view: View) {
        view.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

}