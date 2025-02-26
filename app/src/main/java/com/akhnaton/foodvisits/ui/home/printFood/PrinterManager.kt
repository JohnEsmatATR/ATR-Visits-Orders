package com.akhnaton.foodvisits.ui.home.printFood

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PrinterManager(context: Context) {

    private lateinit var service: SunmiPrinterService
    private var result: Boolean = false


    private val innerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService?) {

            this@PrinterManager.service = service!!
            Log.d("Printer Connected", "onConnected")
        }

        override fun onDisconnected() {
            Log.d("Printer Disconnected", "onDisconnected")
        }

    }

    init {
        result = InnerPrinterManager.getInstance().bindService(
            context,
            innerPrinterCallback
        )
    }

    fun unbindService(context: Context) {
        if (result) {
            InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback)
            result = false
            Log.d("PrinterManager", "Service Unbound")
        }
    }

    suspend fun performTransactionPrintingSuspend(
        view: View
    ): Pair<Int, String?> {
        return suspendCoroutine { coroutine ->
            try {
                service.clearBuffer()

                service.enterPrinterBuffer(true)


                service.printBitmapCustom(scaleImage(convertViewToBitmap(view)), 1,null)
                service.printText("        ", null)

                service.commitPrinterBufferWithCallback(object : InnerResultCallback() {
                    override fun onRunResult(isSuccess: Boolean) {
                        Log.d("Transaction", "Transaction completed: $isSuccess")
                    }

                    override fun onReturnString(result: String?) {
                        Log.d("Transaction", "Transaction return: $result")
                    }

                    override fun onRaiseException(code: Int, msg: String?) {
                        Log.e("Transaction", "Commit failed: $msg")
                        coroutine.resumeWithException(Exception(msg))
                    }

                    override fun onPrintResult(p0: Int, p1: String?) {
                        Log.d("Transaction", "Commit completed with result: $p0, message: $p1")
                    }
                })

                service.exitPrinterBufferWithCallback(true, object : InnerResultCallback() {
                    override fun onRunResult(isSuccess: Boolean) {
                        Log.d("Transaction", "Exited mode: $isSuccess")
                    }

                    override fun onReturnString(result: String?) {
                        Log.d("Transaction", "Exit return: $result")
                    }

                    override fun onRaiseException(code: Int, msg: String?) {
                        Log.e("Transaction", "Exit failed: $msg")
                        coroutine.resumeWithException(Exception(msg))
                    }

                    override fun onPrintResult(p0: Int, p1: String?) {
                        if (p0 == 1) {
                            coroutine.resume(Pair(p0, "Failed to print at transactionIndex"))
                        } else {
                            coroutine.resume(Pair(p0, p1))
                        }
                    }
                })

            } catch (e: RemoteException) {
                Log.e("PrinterManager", "Error in transaction printing: ${e.message}")
                coroutine.resumeWithException(e)
            }
        }
    }

     fun isPrinterStateValid(): Boolean {
        val printerPaperStatus = service.updatePrinterState()
        Log.e("PrinterManager", "Printer is out of paper")
        return printerPaperStatus!=4
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
            Bitmap.createBitmap(
                mView.measuredWidth,
                mView.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
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