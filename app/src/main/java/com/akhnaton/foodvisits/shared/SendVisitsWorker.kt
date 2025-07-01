package com.akhnaton.foodvisits.shared

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akhnaton.foodvisits.data.interfaces.apis.IVisits
import com.akhnaton.foodvisits.domin.CheckConnection

class SendVisitsWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val checkConnection = CheckConnection(appContext)

    override suspend fun doWork(): Result {
        return try {
            val visits = checkConnection.getVisits()

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
                        visit.zoneFlag,
                        visit.checkInDate,
                        visit.dateVisit,
                        visit.customerType,
                        visit.orderType
                    )

                    if (response.status == 200) {

                        checkConnection.deleteSaveVisitFromDB()
                        Log.d("SendVisitsWorker", " : visitarget تم حذف زيارة ${visit.visitarget}")
                    } else {
                        Log.w("SendVisitsWorker", "فشل إرسال زيارة ${visit.customerPartySiteId} - status != 200")
                    }

                } catch (e: Exception) {
                    Log.e("SendVisitsWorker", "خطأ أثناء إرسال زيارة ${visit.customerPartySiteId}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SendVisitsWorker", "فشل في جلب أو إرسال الزيارات: ${e.message}")
            Result.retry()
        }
    }
}

