package com.akhnaton.foodvisits.ui.home.printFood

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.RemoteException
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService

class PrintMe(val context: Context) {


    fun sendTextToPrinter(
        text: String?,
        size: Float,
        isBold: Boolean,
        isUnderLine: Boolean,
        lineBreak: Int
    ) {
        InnerPrinterManager.getInstance().bindService(context, object : InnerPrinterCallback() {
            override fun onConnected(service: SunmiPrinterService) {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()

                try {
                    service.printText(text, object : InnerResultCallback() {
                        override fun onRunResult(isSuccess: Boolean) {
                            Toast.makeText(context, "Run Result", Toast.LENGTH_SHORT).show()

                        }

                        override fun onReturnString(result: String) {
                            Toast.makeText(context, "Return String", Toast.LENGTH_SHORT).show()
                        }

                        override fun onRaiseException(code: Int, msg: String) {
                            Toast.makeText(context, "Raise Exception", Toast.LENGTH_SHORT).show()
                        }

                        override fun onPrintResult(code: Int, msg: String) {
                            Toast.makeText(context, "Print Result", Toast.LENGTH_SHORT).show()
                        }
                    })
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    Toast.makeText(context, "${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onDisconnected() {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun sendViewToPrinter(view: View) {
        InnerPrinterManager.getInstance().bindService(context, object : InnerPrinterCallback() {
            override fun onConnected(service: SunmiPrinterService) {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()

                try {
                    service.printBitmap(scaleImage(convertViewToBitmap(view)), object : InnerResultCallback() {
                        override fun onRunResult(isSuccess: Boolean) {
                            Toast.makeText(context, "Run Result", Toast.LENGTH_SHORT).show()

                        }

                        override fun onReturnString(result: String) {
                            Toast.makeText(context, "Return String", Toast.LENGTH_SHORT).show()
                        }

                        override fun onRaiseException(code: Int, msg: String) {
                            Toast.makeText(context, "Raise Exception", Toast.LENGTH_SHORT).show()
                        }

                        override fun onPrintResult(code: Int, msg: String) {
                            Toast.makeText(context, "Print Result", Toast.LENGTH_SHORT).show()
                        }
                    })
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    Toast.makeText(context, "${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onDisconnected() {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
            }
        })


    }

    fun convertDrawableToBitmap(drawable: Drawable, widthPixels: Int, heightPixels: Int): Bitmap {
        val mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mutableBitmap)
        drawable.setBounds(0, 0, widthPixels, heightPixels)
        drawable.draw(canvas)
        return mutableBitmap
    }


    private fun convertViewToBitmap(mView: View): Bitmap {
        @SuppressLint("Range") val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            ViewGroup.LayoutParams.MATCH_PARENT,
            View.MeasureSpec.UNSPECIFIED
        )
        @SuppressLint("Range") val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            View.MeasureSpec.UNSPECIFIED
        )
        mView.measure(widthMeasureSpec, heightMeasureSpec)
        val b =
            Bitmap.createBitmap(mView.measuredWidth, mView.measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        mView.layout(0, 0, mView.measuredWidth, mView.measuredHeight)
        mView.draw(c)
        return b
    }

    private fun scaleImage(bitmap1: Bitmap): Bitmap {
        val width = bitmap1.width
        val height = bitmap1.height
        val newWidth = 384
        val scaleWidth = newWidth.toFloat() / width.toFloat()
        val matrix = Matrix()
        matrix.postScale(scaleWidth, 1.0f)
        return Bitmap.createBitmap(bitmap1, 0, 0, width, height, matrix, true)
    }


}