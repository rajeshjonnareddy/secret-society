package com.lonley.dev.vault.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lonley.dev.vault.R

object VaultNotificationHelper {

    private const val CHANNEL_ID = "vault_subscription_reminders"
    private const val EXPORT_CHANNEL_ID = "vault_export"
    private const val EXPORT_NOTIFICATION_ID = 9001

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

    fun createExportChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            // Delete old channel in case it was created with IMPORTANCE_LOW
            manager.deleteNotificationChannel(EXPORT_CHANNEL_ID)
            val channel = NotificationChannel(
                EXPORT_CHANNEL_ID,
                "Vault Export",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for vault file exports"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showExportProgress(context: Context, fileName: String) {
        val notification = NotificationCompat.Builder(context, EXPORT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Exporting Vault")
            .setContentText("Saving $fileName to Downloads…")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(EXPORT_NOTIFICATION_ID, notification)
    }

    fun showExportComplete(context: Context, fileName: String, uri: Uri) {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/octet-stream")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, EXPORT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Vault Exported")
            .setContentText("$fileName saved to Downloads")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setProgress(0, 0, false)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(EXPORT_NOTIFICATION_ID, notification)
    }

    fun showExportFailed(context: Context, fileName: String, error: String) {
        val notification = NotificationCompat.Builder(context, EXPORT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Export Failed")
            .setContentText("Could not save $fileName: $error")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setProgress(0, 0, false)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(EXPORT_NOTIFICATION_ID, notification)
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
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Subscription Reminder")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
}
