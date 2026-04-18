package com.example.myapplication.ui.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.notification.SugarGuardNotifications
import com.example.myapplication.viewmodel.LocalAuthViewModel

class ComposeMainActivity : ComponentActivity() {
    private lateinit var authViewModel: LocalAuthViewModel

    // 用于在 onNewIntent 时向 Compose 层传递新 intent 数据
    private var pendingIntent by mutableStateOf<NotificationLaunchInfo?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this)[LocalAuthViewModel::class.java]

        if (!authViewModel.isLoggedIn()) {
            startActivity(Intent(this, ComposeLoginActivity::class.java))
            finish()
            return
        }

        pendingIntent = extractNotificationInfo(intent)

        setContent {
            SugarGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val info = pendingIntent
                    MainNavigationScreen(
                        onBack = {
                            startActivity(Intent(this, ComposeLoginActivity::class.java))
                            finish()
                        },
                        notificationLaunch = info,
                        onNotificationLaunchConsumed = { pendingIntent = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        intent = newIntent
        pendingIntent = extractNotificationInfo(newIntent)
    }

    private fun extractNotificationInfo(intent: Intent?): NotificationLaunchInfo? {
        intent ?: return null
        val open = intent.getBooleanExtra(SugarGuardNotifications.EXTRA_OPEN_NOTIFICATIONS, false)
        if (!open) return null
        val id = intent.getLongExtra(SugarGuardNotifications.EXTRA_NOTIFICATION_ID, -1L)
        val page = intent.getStringExtra(SugarGuardNotifications.EXTRA_TARGET_PAGE)
        return NotificationLaunchInfo(
            notificationId = if (id > 0) id else null,
            targetPage = page
        )
    }
}

/** 通知栏点击时透传的信息 */
data class NotificationLaunchInfo(
    val notificationId: Long?,
    val targetPage: String?
)
