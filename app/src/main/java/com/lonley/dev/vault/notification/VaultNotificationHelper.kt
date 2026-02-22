package com.lonley.dev.vault.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lonley.dev.vault.R

object VaultNotificationHelper {

    private const val CHANNEL_ID = "vault_subscription_reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Subscription Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming subscription renewals"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun sendRenewalNotification(
        context: Context,
        entryName: String,
        daysUntilRenewal: Int,
        notificationId: Int
    ) {
        val text = if (daysUntilRenewal <= 0) {
            "$entryName renews today!"
        } else {
            "$entryName renews in $daysUntilRenewal day${if (daysUntilRenewal == 1) "" else "s"}"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Subscription Reminder")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
}
