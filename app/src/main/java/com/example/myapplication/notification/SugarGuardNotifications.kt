package com.example.myapplication.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.ui.compose.ComposeMainActivity

object SugarGuardNotifications {
    const val CHANNEL_ID = "sugarguard_server"

    // Intent extra keys
    const val EXTRA_OPEN_NOTIFICATIONS = "open_notifications"
    const val EXTRA_NOTIFICATION_ID = "notification_id"
    const val EXTRA_TARGET_PAGE = "notification_target_page"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = mgr.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val ch = NotificationChannel(
            CHANNEL_ID,
            "糖知提醒",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "服务端同步的控糖与记录提醒"
            enableVibration(true)
        }
        mgr.createNotificationChannel(ch)
    }

    /**
     * 展示来自服务端的通知。
     * @param id 服务端 notification id
     * @param title 标题
     * @param content 正文
     * @param createdAtMillis 服务端创建时间（毫秒）用于显示准确的发送时间；传 null 则用当前时间
     * @param targetPage 目标页面（diary/analysis/health_record/...）
     */
    fun showFromServer(
        context: Context,
        id: Long,
        title: String,
        content: String,
        createdAtMillis: Long? = null,
        targetPage: String? = null
    ) {
        ensureChannel(context)
        val launch = Intent(context, ComposeMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_NOTIFICATIONS, true)
            putExtra(EXTRA_NOTIFICATION_ID, id)
            if (!targetPage.isNullOrBlank()) {
                putExtra(EXTRA_TARGET_PAGE, targetPage)
            }
        }
        val reqCode = (id % Int.MAX_VALUE).toInt().coerceAtLeast(1)
        val pi = PendingIntent.getActivity(
            context,
            reqCode,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val whenMillis = createdAtMillis ?: System.currentTimeMillis()
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setWhen(whenMillis)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(reqCode, n)
    }
}
