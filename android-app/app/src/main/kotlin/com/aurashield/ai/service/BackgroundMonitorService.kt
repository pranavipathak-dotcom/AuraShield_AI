package com.aurashield.ai.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aurashield.ai.AuraShieldApp
import com.aurashield.ai.MainActivity
import kotlinx.coroutines.*

class BackgroundMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopServiceInternal()
            return START_NOT_STICKY
        }

        if (!isRunning) {
            isRunning = true
            startForegroundServiceCompat()
            startMonitoringLoop()
        }

        return START_STICKY
    }

    private fun startForegroundServiceCompat() {
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires specifying foregroundServiceType
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val stopIntent = Intent(this, BackgroundMonitorService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Custom notification layout / styling
        return NotificationCompat.Builder(this, AuraShieldApp.CHANNEL_ID)
            .setContentTitle("AuraShield AI Monitor Active")
            .setContentText("Performing background AI heuristics...")
            // Use system icon for notification for reliable compilation without external assets
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Monitoring",
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            // Verify and demonstrate link resolution of ML runtimes
            initMLRuntimes()
            
            while (isActive) {
                Log.d(TAG, "AI background monitor performing inference tick...")
                // In a production app, fetch sensor data/inputs and invoke TFLite or ONNX runtimes.
                // e.g.:
                // val result = runDummyInference()
                // Log.d(TAG, "Inference outcome: $result")
                delay(5000) // periodic interval
            }
        }
    }

    /**
     * Touch TensorFlow Lite and ONNX Mobile Runtime APIs to guarantee linking compiles correctly.
     */
    private fun initMLRuntimes() {
        try {
            // Touch TensorFlow Lite API
            val tfVersion = org.tensorflow.lite.TensorFlowLite.version()
            Log.i(TAG, "TFLite runtime loaded successfully. Version: $tfVersion")
            
            // Touch ONNX Runtime API
            val ortEnv = ai.onnxruntime.OrtEnvironment.getEnvironment()
            Log.i(TAG, "ONNX Runtime environment initialized successfully. Platform: Android")
        } catch (e: NoClassDefFoundError) {
            Log.e(TAG, "ML Runtime classes not found on classpath. Check your gradle dependencies: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ML runtimes: ${e.message}", e)
        }
    }

    private fun stopServiceInternal() {
        Log.d(TAG, "Stopping service internally")
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        serviceScope.cancel() // cancel all coroutines
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't bind to this service
    }

    companion object {
        private const val TAG = "AuraShieldService"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_STOP_SERVICE = "com.aurashield.ai.action.STOP_SERVICE"
    }
}
