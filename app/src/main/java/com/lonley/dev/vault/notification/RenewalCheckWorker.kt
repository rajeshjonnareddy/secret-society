package com.lonley.dev.vault.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RenewalCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("vault_reminders", Context.MODE_PRIVATE)
        val allEntries = prefs.all

        val now = System.currentTimeMillis()
        val threeDaysMs = 3 * 86_400_000L
        var notificationId = 1000

        for ((key, value) in allEntries) {
            if (!key.endsWith("_nextRenewal")) continue
            val nextRenewal = (value as? Long) ?: continue
            val entryId = key.removeSuffix("_nextRenewal")
            val entryName = prefs.getString("${entryId}_name", null) ?: continue

            val diff = nextRenewal - now
            if (diff in 0..threeDaysMs) {
                val daysUntil = (diff / 86_400_000L).toInt()
                VaultNotificationHelper.sendRenewalNotification(
                    applicationContext,
                    entryName,
                    daysUntil,
                    notificationId++
                )
            }
        }

        return Result.success()
    }
}
