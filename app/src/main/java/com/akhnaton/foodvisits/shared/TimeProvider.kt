package com.akhnaton.foodvisits.shared

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.akhnaton.foodvisits.R
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class RealTimeService : Service() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val channelId = "REAL_TIME_CHANNEL"
    private val notificationId = 1001

    private var startTimeMillis: Long = 0L
    private var startElapsedRealtime: Long = 0L
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(notificationId, buildNotification("Loading..."))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverTime = fetchServerTimeMillis()
                startTimeMillis = serverTime
                startElapsedRealtime = SystemClock.elapsedRealtime()

                val serverTimeInSeconds = serverTime / 1000
                SharedPrefsHelper.saveServerUnixTime(this@RealTimeService, serverTimeInSeconds)
                startTickingLoop()
                Log.e("RealTimeService", "Failed to fetch time: ${serverTimeInSeconds}")
            } catch (e: Exception) {
                Log.e("RealTimeService", "Failed to fetch time: ${e.message}")
            }
        }
    }

    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Real Time Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.setSound(null, null)
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(time: String): Notification {
        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("⏱ Server Time")
            .setContentText("Time: $time")
            .setSmallIcon(R.drawable.ic_logo_foreground)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    private fun updateNotification(time: String) {
        notificationBuilder.setContentText("Time: $time")
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private suspend fun fetchServerTimeMillis(): Long {
        return withContext(Dispatchers.IO) {
            val url = URL("https://timeapi.io/api/Time/current/zone?timeZone=Africa/Cairo")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 50000
            conn.requestMethod = "GET"

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val datetime = json.getString("dateTime")

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("Africa/Cairo")
            val date = sdf.parse(datetime) ?: throw Exception("Failed to parse time")
            date.time
        }
    }

    private fun startTickingLoop() {
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val elapsed = SystemClock.elapsedRealtime() - startElapsedRealtime
                val currentTimeMillis = startTimeMillis + elapsed
                val formatted = formatTime(currentTimeMillis)

                updateNotification(formatted)
                SharedPrefsHelper.saveServerTime(this@RealTimeService, formatted)

                delay(1000)
            }
        }
    }

    private fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Africa/Cairo")
        return sdf.format(Date(timeMillis))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}
