package com.akhnaton.foodvisits.ui.home.printFood

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import android.view.View
import androidx.core.graphics.createBitmap
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService
import java.io.File
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

    suspend fun printBitmapSuspend(bitmap: Bitmap): Pair<Int, String?> {
        return suspendCoroutine { coroutine ->
            try {
                service.clearBuffer()
                service.enterPrinterBuffer(true)

                service.printBitmapCustom(bitmap, 1, null)
                service.printText("        ", null)

                service.commitPrinterBufferWithCallback(object : InnerResultCallback() {
                    override fun onRunResult(isSuccess: Boolean) {}
                    override fun onReturnString(result: String?) {}
                    override fun onRaiseException(code: Int, msg: String?) {
                        coroutine.resumeWithException(Exception(msg))
                    }
                    override fun onPrintResult(p0: Int, p1: String?) {
                        if (p0 == 1) {
                            coroutine.resume(Pair(p0, "Failed"))
                        } else {
                            coroutine.resume(Pair(p0, p1))
                        }
                    }
                })

                service.exitPrinterBufferWithCallback(true, object : InnerResultCallback() {
                    override fun onRunResult(isSuccess: Boolean) {}
                    override fun onReturnString(result: String?) {}
                    override fun onRaiseException(code: Int, msg: String?) {
                        coroutine.resumeWithException(Exception(msg))
                    }
                    override fun onPrintResult(p0: Int, p1: String?) {}
                })
            } catch (e: Exception) {
                coroutine.resumeWithException(e)
            }
        }
    }





}