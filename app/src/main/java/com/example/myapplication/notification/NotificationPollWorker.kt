package com.example.myapplication.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.util.NotificationTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationPollWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val authPrefs = applicationContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val userId = authPrefs.getLong("user_id", 0L)
            if (userId <= 0L) return@withContext Result.success()

            val pollPrefs = applicationContext.getSharedPreferences("notif_poll", Context.MODE_PRIVATE)
            val lastMax = pollPrefs.getLong("last_max_id", 0L)
            val baselineDone = pollPrefs.getBoolean("notif_baseline_done", false)
            val resp = RetrofitClient.getNotificationApiService().getNotifications(userId).execute()
            if (!resp.isSuccessful || resp.body()?.isSuccess != true) return@withContext Result.retry()

            val list = resp.body()?.data ?: emptyList()
            val maxInBatch = list.mapNotNull { it.id }.maxOrNull() ?: 0L

            if (!baselineDone) {
                pollPrefs.edit()
                    .putLong("last_max_id", maxInBatch.coerceAtLeast(lastMax))
                    .putBoolean("notif_baseline_done", true)
                    .apply()
                return@withContext Result.success()
            }

            // 客户端本地开关：若用户关闭某类提醒，不弹系统通知
            val appSettings = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

            var maxId = lastMax
            for (n in list) {
                val id = n.id ?: continue
                if (id > lastMax && n.isRead != true && isTypeEnabled(n.type, appSettings)) {
                    val millis = NotificationTimeFormatter.parseToMillis(n.createdAt)
                    SugarGuardNotifications.showFromServer(
                        context = applicationContext,
                        id = id,
                        title = n.title ?: "糖知",
                        content = n.content ?: "",
                        createdAtMillis = millis,
                        targetPage = n.targetPage
                    )
                }
                if (id > maxId) maxId = id
            }
            pollPrefs.edit().putLong("last_max_id", maxOf(maxId, maxInBatch)).apply()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun isTypeEnabled(type: String?, prefs: android.content.SharedPreferences): Boolean {
        val t = type ?: return true
        return when {
            t.startsWith("meal_reminder") -> prefs.getBoolean("meal_reminder", true)
            t == "record_reminder" -> prefs.getBoolean("record_reminder", true)
            t == "sugar_alert" -> prefs.getBoolean("sugar_alert", true)
            t == "water_reminder" -> prefs.getBoolean("water_reminder", true)
            t == "weekly_report" -> prefs.getBoolean("weekly_report", false)
            else -> true
        }
    }
}
