package com.akhnaton.foodvisits.shared

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.akhnaton.foodvisits.R

object WaveHelper {

    private const val TAG = "WaveHelper"

    fun goToApp(
        context: Context,
        packageName: String
    ) {

        Log.d(
            TAG,
            "goToApp() called"
        )

        Log.d(
            TAG,
            "Target packageName = $packageName"
        )

        val packageManager =
            context.packageManager

        try {

            val launchIntent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            Log.d(
                TAG,
                "Launch intent = $launchIntent"
            )

            if (launchIntent != null) {

                Log.d(
                    TAG,
                    "Target app detected. Launching: $packageName"
                )

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(
                    launchIntent
                )

                Log.d(
                    TAG,
                    "Target app launched successfully: $packageName"
                )

            } else {

                Log.w(
                    TAG,
                    "No launch intent found for package: $packageName"
                )

                Log.w(
                    TAG,
                    "The app may not be installed, the package name may be wrong, or package visibility may not be configured."

                )

                showDialogMessageError(
                    context = context,
                    message = "التطبيق ليس مثبتاً، يمكنك الذهاب الى Play Store لتنزيله",
                    onOkClick = {

                        Log.d(
                            TAG,
                            "User clicked OK. Opening Play Store for: $packageName"
                        )

                        installFromPlayStore(
                            context,
                            packageName
                        )
                    }
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error while trying to open package: $packageName",
                e
            )

            showDialogMessageError(
                context = context,
                message = "حدث خطأ أثناء محاولة فتح التطبيق",
                onOkClick = {
                    installFromPlayStore(
                        context,
                        packageName
                    )
                }
            )
        }
    }

    fun showDialogMessageError(
        context: Context,
        message: String,
        onOkClick: () -> Unit = {}
    ) {

        Log.d(
            TAG,
            "showDialogMessageError() called. message=$message"
        )

        showDialog(
            context = context,
            title = "Warning!",
            message = message,
            isCancelable = true
        )
            .setPositiveButton("Ok") {
                    dialog: DialogInterface,
                    _: Int ->

                Log.d(
                    TAG,
                    "Warning dialog OK clicked"
                )

                onOkClick()

                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_error)
            .setCancelable(false)
            .create()
            .show()
    }

    private fun installFromPlayStore(
        context: Context,
        packageName: String
    ) {

        Log.d(
            TAG,
            "Opening Play Store. packageName=$packageName"
        )

        val uri =
            Uri.parse(
                "https://play.google.com/store/apps/details?id=$packageName"
            )

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                uri
            )

        try {

            context.startActivity(
                intent,
                null
            )

            Log.d(
                TAG,
                "Play Store intent started successfully"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Failed to open Play Store. packageName=$packageName",
                e
            )
        }
    }

    fun showDialog(
        context: Context,
        title: String,
        message: String,
        isCancelable: Boolean
    ): AlertDialog.Builder {

        Log.d(
            TAG,
            "showDialog() title=$title, message=$message, isCancelable=$isCancelable"
        )

        val builder =
            AlertDialog.Builder(context)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(isCancelable)

        return builder
    }
}