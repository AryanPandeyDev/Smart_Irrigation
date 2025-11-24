package com.example.smartirrigation.presentation.dashboard.foreground_service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartirrigation.MainActivity
import com.example.smartirrigation.R
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PumpStatusService : Service() {

    @Inject
    lateinit var repository: IrrigationRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var lastPumpStatus: Boolean? = null

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
        observePumpStatus()
    }

    private fun observePumpStatus() {
        serviceScope.launch {
            Log.d("PumpStatusService", "Starting to observe pump status")
            repository.getStatus().collect { info ->
                Log.d("PumpStatusService", "Received info: $info")
                info?.let {
                    val currentStatus = it.relayStatus
                    Log.d("PumpStatusService", "Current Status: $currentStatus, Last Status: $lastPumpStatus")
                    
                    if (lastPumpStatus != null && lastPumpStatus != currentStatus) {
                        Log.d("PumpStatusService", "Status changed! Showing notification.")
                        showNotification(currentStatus)
                    } else {
                        Log.d("PumpStatusService", "No change or initial state.")
                    }
                    lastPumpStatus = currentStatus
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceJob.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        start()
        return START_STICKY
    }

    private fun start() {
        val notification = createNotification("Receiving pump status")
        startForeground(1, notification)
    }

    private fun showNotification(isPumpOn: Boolean) {
        val statusText = if (isPumpOn) "Pump turned ON" else "Pump turned OFF"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, createNotification(statusText, "pump_alerts"))
    }

    private fun createNotification(content: String, channelId: String = "pump_status"): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pump Status")
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(if (channelId == "pump_alerts") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_MIN)
            .build()
    }

}