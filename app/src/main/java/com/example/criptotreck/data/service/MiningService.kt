package com.example.criptotreck.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MiningService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var miningJob: Job? = null

    companion object {
        const val CHANNEL_ID = "mining_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_COIN_NAME = "EXTRA_COIN_NAME"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val coinName = intent.getStringExtra(EXTRA_COIN_NAME) ?: "Crypto"
                startMining(coinName)
            }

            ACTION_STOP -> {
                stopMining()
            }
        }
        return START_NOT_STICKY
    }

    private fun startMining(coinName: String) {
        if (miningJob?.isActive == true) return
        val initialNotification = buildNotification(coinName, 0.0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        miningJob = serviceScope.launch {
            var minedAmount = 0.0
            val manager = getSystemService(NotificationManager::class.java)

            while (isActive) {
                delay(1000L)
                minedAmount += 0.0005
                manager?.notify(NOTIFICATION_ID, buildNotification(coinName, minedAmount))
            }
        }
    }

    private fun stopMining() {
        miningJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(coinName: String, amount: Double): Notification {
        val stopIntent = Intent(this, MiningService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val formattedAmount = String.format("%.4f", amount)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Майнинг $coinName активен")
            .setContentText("Добыто: $formattedAmount $coinName")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Остановить", stopPendingIntent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Фоновый майнинг",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
