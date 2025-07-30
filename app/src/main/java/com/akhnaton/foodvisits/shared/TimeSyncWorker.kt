package com.akhnaton.foodvisits.shared

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.domin.AppSettingRepository

class TimeSyncWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            val repository = AppSettingRepository()
            val appSettingResponse = repository.getAppSetting(BuildConfig.VERSION_NAME)
            val serverTime = appSettingResponse.data.time
            val mobileTime = System.currentTimeMillis() / 1000
            val diffInSeconds = kotlin.math.abs(serverTime - mobileTime)

            SharedPreferencesHelper.getInstance().saveTimeDifference(diffInSeconds)

            Log.d("TimeSyncWorker", " Time diff saved: $diffInSeconds")
            Result.success()
        } catch (e: Exception) {
            Log.e("TimeSyncWorker", "Error syncing time: ${e.message}")
            Result.retry()
        }
    }
}
