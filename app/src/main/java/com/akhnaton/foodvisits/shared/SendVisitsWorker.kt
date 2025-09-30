package com.akhnaton.foodvisits.shared

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.interfaces.apis.IVisits
import com.akhnaton.foodvisits.domin.CheckConnection

class SendVisitsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val checkConnection = CheckConnection(appContext)

    companion object {
        const val CHANNEL_ID = "visits_channel"
        const val FOREGROUND_NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        val visits = try {
            checkConnection.getVisits()
        } catch (e: Exception) {
            Log.e("SendVisitsWorker", "فشل في جلب الزيارات: ${e.message}")
            return Result.retry()
        }
        if (visits.isEmpty()) {
            Log.d("SendVisitsWorker", "لا توجد زيارات لإرسالها، إنهاء العمل بدون إشعار")
            return Result.success()
        }
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("جاري إرسال الزيارات")
            .setSmallIcon(R.drawable.ic_logo_foreground)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(visits.size, 0, false)


        setForegroundAsync(
            ForegroundInfo(
                FOREGROUND_NOTIFICATION_ID,
                builder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )

        var sentCount = 0

        for (visit in visits) {
            try {
                val response = RetrofitClient.getInstance(IVisits::class.java).saveVisits(
                    visit.version,
                    visit.token,
                    visit.customerPartySiteId,
                    visit.visitType,
                    visit.visitarget,
                    visit.visitActualTarget,
                    visit.latitude,
                    visit.longitude,
                    visit.deviceType,
                    visit.startLat,
                    visit.startLong,
                    visit.zoneFlag,
                    visit.checkInDate,
                    visit.dateVisit,
                    visit.customerType,
                    visit.orderType
                )

                if (response.status == 200) {
                    sentCount++
                    checkConnection.deleteSaveVisitFromDB()
                    Log.d("SendVisitsWorker", "تم إرسال زيارة ${visit.visitarget}")
                } else {
                    Log.w("SendVisitsWorker", "فشل إرسال زيارة ${visit.customerPartySiteId}")
                }

                builder.setContentText("تم إرسال $sentCount من ${visits.size} زيارة")
                    .setProgress(visits.size, sentCount, false)
                notificationManager.notify(FOREGROUND_NOTIFICATION_ID, builder.build())

            } catch (e: Exception) {
                Log.e("SendVisitsWorker", "خطأ أثناء إرسال زيارة ${visit.customerPartySiteId}: ${e.message}")
            }
        }

        builder.setContentTitle("تم الإرسال")
            .setContentText("تم إرسال $sentCount من ${visits.size} زيارة بنجاح")
            .setProgress(0, 0, false)
            .setOngoing(false)

        showFinalSuccessNotification(sentCount, visits.size)

        return Result.success()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Visits Upload Channel"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun showFinalSuccessNotification(sent: Int, total: Int) {
        val resultText = "تم إرسال $sent من $total زيارة بنجاح"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("انتهى  إرسال الزيارات المحفوظه محليا ")
            .setContentText(resultText)
            .setSmallIcon(R.drawable.ic_logo_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.notify(100, notification)
    }

}
