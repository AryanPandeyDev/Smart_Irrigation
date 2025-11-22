package com.example.smartirrigation.presentation.dashboard.foreground_service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smartirrigation.R

class PumpStatusService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        @Volatile
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        start()
        return START_STICKY
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this,"pump_status")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pump Status")
            .setContentText("Receiving pump status")
            .build()
        startForeground(1,notification)
    }

}