package com.aurashield.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class AuraShieldApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AuraShield AI Background Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification channel for the background AI runtime monitor"
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "aurashield_ai_monitor_channel"
    }
}
