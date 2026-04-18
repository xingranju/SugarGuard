package com.example.myapplication.ui.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.UserHealthProfile
import com.example.myapplication.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)
    val username = prefs.getString("username", "糖知用户") ?: "糖知用户"

    var sugarTarget by remember { mutableFloatStateOf(25f) }
    var daysCount by remember { mutableIntStateOf(0) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf(username) }

    LaunchedEffect(userId) {
        val userInfoApi = RetrofitClient.getUserInfoApiService()
        val user: UserInfo? = withContext(Dispatchers.IO) {
            try {
                val resp = userInfoApi.getUserInfo().execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
            } catch (_: Exception) {
                null
            }
        }
        avatarUrl = user?.avatarUrl
        displayName = prefs.getString("username", user?.username ?: username) ?: username

        val profileApi = RetrofitClient.getUserProfileApiService()
        val profile: UserHealthProfile? = withContext(Dispatchers.IO) {
            try {
                val resp = profileApi.getHealthProfile().execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
            } catch (_: Exception) {
                null
            }
        }
        sugarTarget = profile?.sugarLimit ?: 25f
        daysCount = try {
            val healthApi = com.example.myapplication.api.RetrofitClient.getDailyHealthRecordApiService()
            val countResp = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                healthApi.getRecordCount().execute()
            }
            if (countResp.isSuccessful && countResp.body()?.isSuccess == true) {
                (countResp.body()?.data ?: 0).toInt()
            } else 0
        } catch (_: Exception) {
            0
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUserEdit by remember { mutableStateOf(false) }
    var showHealthProfile by remember { mutableStateOf(false) }
    var showDrinkPreference by remember { mutableStateOf(false) }
    var showDailyHealthRecord by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var showHistoryScan by remember { mutableStateOf(false) }

    if (showUserEdit) { LocalUserEditScreen(onBack = { showUserEdit = false }); return }
    if (showHealthProfile) { HealthProfileScreen(onBack = { showHealthProfile = false }); return }
    if (showDailyHealthRecord) { DailyHealthRecordScreen(onBack = { showDailyHealthRecord = false }); return }
    if (showDrinkPreference) { DrinkPreferenceScreen(onBack = { showDrinkPreference = false }); return }
    if (showNotifications) { ReminderSettingsScreen(onBack = { showNotifications = false }); return }
    if (showSettings) { SettingsScreen(onBack = { showSettings = false }); return }
    if (showHelp) { HelpScreen(onBack = { showHelp = false }); return }
    if (showFeedback) { FeedbackScreen(onBack = { showFeedback = false }); return }
    if (showAchievements) { AchievementsScreen(onBack = { showAchievements = false }); return }
    if (showReport) { ReportScreen(onBack = { showReport = false }); return }
    if (showHistoryScan) { HistoryScanScreen(onBack = { showHistoryScan = false }); return }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // User info card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = avatarUrl,
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MintBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.firstOrNull()?.toString() ?: "U",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    Text("今日目标 ${sugarTarget.toInt()}g · 已记录 $daysCount 天", fontSize = 12.sp, color = Color(0xFFBDBDBD))
                }

                Surface(
                    modifier = Modifier.clickable { showUserEdit = true },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFAFAFA)
                ) {
                    Text(
                        "编辑资料",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Health section
        ProfileMenuGroup(
            modifier = Modifier.padding(horizontal = 24.dp),
            items = listOf(
                ProfileItem(Icons.Default.Badge, "健康档案", MintBg, MintGreen) { showHealthProfile = true },
                ProfileItem(Icons.Default.FavoriteBorder, "每日健康数据", Color(0xFFE3F2FD), Color(0xFF64B5F6)) { showDailyHealthRecord = true },
                ProfileItem(Icons.Default.LocalCafe, "饮品偏好管理", Color(0xFFFFF3E0), Color(0xFFFFB74D)) { showDrinkPreference = true },
                ProfileItem(Icons.Default.NotificationsActive, "提醒设置", Color(0xFFF3E5F5), Color(0xFFBA68C8)) { showNotifications = true },
                ProfileItem(Icons.Default.EmojiEvents, "控糖成就", Color(0xFFFFF8E1), Color(0xFFFFCA28), "已连续达标 7 天") { showAchievements = true },
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuGroup(
            modifier = Modifier.padding(horizontal = 24.dp),
            items = listOf(
                ProfileItem(Icons.Default.Assessment, "报告历史", Color(0xFFE8F5E9), Color(0xFF66BB6A)) { showReport = true },
                ProfileItem(Icons.Default.History, "历史扫描记录", Color(0xFFFCE4EC), Color(0xFFEC407A)) { showHistoryScan = true },
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuGroup(
            modifier = Modifier.padding(horizontal = 24.dp),
            items = listOf(
                ProfileItem(Icons.Default.Settings, "设置", Color(0xFFF5F5F5), Color(0xFFBDBDBD)) { showSettings = true },
                ProfileItem(Icons.Default.HelpOutline, "帮助与反馈", Color(0xFFF5F5F5), Color(0xFFBDBDBD)) { showHelp = true },
                ProfileItem(Icons.Default.ExitToApp, "退出登录", Color(0xFFFFEBEE), Color(0xFFEF5350)) { showLogoutDialog = true },
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出", fontWeight = FontWeight.Bold) },
            text = { Text("确定要退出登录吗?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        prefs.edit().clear().apply()
                        val intent = Intent(context, ComposeLoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("退出") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("取消") }
            }
        )
    }
}

data class ProfileItem(
    val icon: ImageVector,
    val title: String,
    val iconBg: Color,
    val iconColor: Color,
    val subtitle: String? = null,
    val onClick: () -> Unit
)

@Composable
private fun ProfileMenuGroup(modifier: Modifier = Modifier, items: List<ProfileItem>) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = item.iconBg
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(item.icon, contentDescription = item.title, tint = item.iconColor, modifier = Modifier.size(20.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555))
                        item.subtitle?.let {
                            Text(it, fontSize = 10.sp, color = Color(0xFFBDBDBD))
                        }
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFD0D0D0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (index < items.size - 1) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF5F5F5)
                    )
                }
            }
        }
    }
}
