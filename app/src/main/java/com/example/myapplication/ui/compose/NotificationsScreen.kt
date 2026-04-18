package com.example.myapplication.ui.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.NotificationDto
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.util.NotificationTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNavigateTo: ((String) -> Unit)? = null,
    initialHighlightId: Long? = null
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf(listOf<NotificationDto>()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadNotifications() {
        scope.launch {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                try {
                    val resp = RetrofitClient.getNotificationApiService().getNotifications(userId).execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
                } catch (_: Exception) { null }
            }
            notifications = result ?: emptyList()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadNotifications() }

    // 从系统通知栏打开时，若携带 initialHighlightId，立即为该通知调用后端标记已读
    LaunchedEffect(initialHighlightId) {
        val id = initialHighlightId ?: return@LaunchedEffect
        if (id <= 0L) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                RetrofitClient.getNotificationApiService().markAsRead(id, userId).execute()
            }
        }
        notifications = notifications.map { if (it.id == id) it.copy(isRead = true) else it }
    }

    fun markRead(id: Long) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try { RetrofitClient.getNotificationApiService().markAsRead(id, userId).execute() } catch (_: Exception) {}
            }
            notifications = notifications.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllRead() {
        scope.launch {
            withContext(Dispatchers.IO) {
                try { RetrofitClient.getNotificationApiService().markAllAsRead(userId).execute() } catch (_: Exception) {}
            }
            notifications = notifications.map { it.copy(isRead = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("消息通知", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800,
                modifier = Modifier.weight(1f))

            val unreadCount = notifications.count { it.isRead != true }
            if (unreadCount > 0) {
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { markAllRead() },
                    shape = RoundedCornerShape(12.dp), color = MintBg
                ) {
                    Text("全部已读", fontSize = 12.sp, color = MintGreen, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("\uD83D\uDD14", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("暂无通知", fontSize = 14.sp, color = Gray400)
                }
            }
        } else {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val unread = notifications.filter { it.isRead != true }
                val read = notifications.filter { it.isRead == true }

                if (unread.isNotEmpty()) {
                    Text("未读消息 (${unread.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    unread.forEach { n ->
                        NotificationCard(n) {
                            markRead(n.id ?: return@NotificationCard)
                            n.targetPage?.let { page -> onNavigateTo?.invoke(page) ?: onBack() } ?: onBack()
                        }
                    }
                }

                if (read.isNotEmpty()) {
                    if (unread.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                    Text("已读消息", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gray400)
                    read.forEach { n ->
                        NotificationCard(n) {
                            n.targetPage?.let { page -> onNavigateTo?.invoke(page) ?: onBack() } ?: onBack()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationDto, onClick: () -> Unit) {
    val isRead = notification.isRead == true
    val type = notification.type.orEmpty()
    val accentColor = when {
        type == "sugar_alert" -> RedHigh
        type == "record_reminder" -> OrangeMid
        type.startsWith("meal_reminder") -> OrangeMid
        type == "water_reminder" -> Color(0xFF42A5F5)
        type == "weekly_report" -> MintGreen
        else -> MintGreen
    }
    val icon = when {
        type == "sugar_alert" -> "⚠\uFE0F"
        type == "record_reminder" -> "\uD83D\uDCDD"
        type.startsWith("meal_reminder") -> "\uD83C\uDF7D\uFE0F"
        type == "water_reminder" -> "\uD83D\uDCA7"
        type == "weekly_report" -> "\uD83D\uDCCA"
        else -> "\uD83D\uDD14"
    }
    val linkText = when (notification.targetPage) {
        "analysis" -> "查看分析 →"
        "diary" -> "去记录 →"
        "health_record" -> "查看健康记录 →"
        "recognition" -> "去识别 →"
        "chat" -> "去问问 →"
        "profile" -> "查看我的 →"
        else -> "查看详情 →"
    }
    val timeLabel = NotificationTimeFormatter.formatRelative(notification.createdAt)

    Surface(
        modifier = Modifier.fillMaxWidth().alpha(if (isRead) 0.65f else 1f).clickable { onClick() },
        shape = RoundedCornerShape(20.dp), color = Color.White,
        shadowElevation = if (isRead) 0.dp else 1.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            if (!isRead) {
                Box(modifier = Modifier.width(4.dp).fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)))
            }
            Row(modifier = Modifier.weight(1f).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(icon, fontSize = 20.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(notification.title ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                        Text(timeLabel, fontSize = 10.sp, color = Gray400)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(notification.content ?: "", fontSize = 12.sp, color = Color(0xFF999999), lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(linkText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }
            }
        }
    }
}

