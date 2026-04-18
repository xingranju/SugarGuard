package com.example.myapplication.ui.compose

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.NotificationSettingsDto
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReminderSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = authPrefs.getLong("user_id", 1L)
    val scope = rememberCoroutineScope()

    // 本地缓存作为首次加载 & NotificationPollWorker 过滤使用
    var mealReminder by remember { mutableStateOf(prefs.getBoolean("meal_reminder", true)) }
    var recordReminder by remember { mutableStateOf(prefs.getBoolean("record_reminder", true)) }
    var waterReminder by remember { mutableStateOf(prefs.getBoolean("water_reminder", true)) }
    var sugarAlert by remember { mutableStateOf(prefs.getBoolean("sugar_alert", true)) }
    var weeklyReport by remember { mutableStateOf(prefs.getBoolean("weekly_report", false)) }
    var quietStart by remember { mutableStateOf(prefs.getString("quiet_start", "22:00") ?: "22:00") }
    var quietEnd by remember { mutableStateOf(prefs.getString("quiet_end", "08:00") ?: "08:00") }

    var syncing by remember { mutableStateOf(true) }

    // 从后端加载最新设置（覆盖本地缓存），失败时保留本地值
    LaunchedEffect(userId) {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val resp = RetrofitClient.getNotificationSettingsApiService()
                    .getSettings(userId).execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
            }.getOrNull()
        }
        if (result != null) {
            mealReminder = result.mealReminder ?: mealReminder
            recordReminder = result.recordReminder ?: recordReminder
            waterReminder = result.waterReminder ?: waterReminder
            sugarAlert = result.sugarAlert ?: sugarAlert
            weeklyReport = result.weeklyReport ?: weeklyReport
            quietStart = result.quietStart ?: quietStart
            quietEnd = result.quietEnd ?: quietEnd
            prefs.edit()
                .putBoolean("meal_reminder", mealReminder)
                .putBoolean("record_reminder", recordReminder)
                .putBoolean("water_reminder", waterReminder)
                .putBoolean("sugar_alert", sugarAlert)
                .putBoolean("weekly_report", weeklyReport)
                .putString("quiet_start", quietStart)
                .putString("quiet_end", quietEnd)
                .apply()
        }
        syncing = false
    }

    fun persistLocal(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun pushToBackend() {
        scope.launch {
            val dto = NotificationSettingsDto(
                userId = userId,
                sugarAlert = sugarAlert,
                recordReminder = recordReminder,
                mealReminder = mealReminder,
                waterReminder = waterReminder,
                weeklyReport = weeklyReport,
                quietStart = quietStart,
                quietEnd = quietEnd
            )
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val resp = RetrofitClient.getNotificationSettingsApiService()
                        .updateSettings(userId, dto).execute()
                    resp.isSuccessful && resp.body()?.isSuccess == true
                }.getOrDefault(false)
            }
            if (!ok) {
                Toast.makeText(context, "通知设置暂存本地，网络恢复后自动同步", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回", tint = Gray600) }
            Spacer(modifier = Modifier.weight(1f))
            Text("提醒设置", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }
        if (syncing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                color = MintGreen
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 饮食记录提醒（早中晚）
            ReminderCard(
                title = "饮食记录提醒",
                desc = "早、中、晚餐时间段未记录会提醒你",
                checked = mealReminder,
                onChange = {
                    mealReminder = it
                    persistLocal("meal_reminder", it)
                    pushToBackend()
                }
            ) {
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF5F5F5))
                Text("提醒时间", fontSize = 12.sp, color = Gray400)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("早餐 7:30-9:30", "午餐 11:30-13:30", "晚餐 18:00-20:30").forEach { label ->
                        Surface(shape = RoundedCornerShape(8.dp), color = MintBg) {
                            Text(label, fontSize = 12.sp, color = MintGreen, fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
            }

            // 健康记录提醒
            ReminderCard(
                title = "健康记录提醒",
                desc = "晚上仍未做任何记录时提醒你补录",
                checked = recordReminder,
                onChange = {
                    recordReminder = it
                    persistLocal("record_reminder", it)
                    pushToBackend()
                }
            )

            // 饮水提醒
            ReminderCard(
                title = "饮水提醒",
                desc = "白天每 2 小时提醒一次补充水分",
                checked = waterReminder,
                onChange = {
                    waterReminder = it
                    persistLocal("water_reminder", it)
                    pushToBackend()
                }
            )

            // 糖分超标预警
            ReminderCard(
                title = "糖分超标预警",
                desc = "达到目标 80% 时提醒，超标时再次提醒",
                checked = sugarAlert,
                onChange = {
                    sugarAlert = it
                    persistLocal("sugar_alert", it)
                    pushToBackend()
                }
            )

            // 周报推送
            ReminderCard(
                title = "周报推送",
                desc = "每周日推送控糖周报",
                checked = weeklyReport,
                onChange = {
                    weeklyReport = it
                    persistLocal("weekly_report", it)
                    pushToBackend()
                }
            )

            // 勿扰时段
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("勿扰时段", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("在该时段内不会推送记录/饮水等提醒（糖分预警仍会生效）", fontSize = 12.sp, color = Gray400)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("开始", fontSize = 14.sp, color = Gray700)
                        QuietTimePicker(value = quietStart, onChange = {
                            quietStart = it
                            prefs.edit().putString("quiet_start", it).apply()
                            pushToBackend()
                        })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("结束", fontSize = 14.sp, color = Gray700)
                        QuietTimePicker(value = quietEnd, onChange = {
                            quietEnd = it
                            prefs.edit().putString("quiet_end", it).apply()
                            pushToBackend()
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    desc: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    extra: @Composable (() -> Unit)? = null
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, fontSize = 12.sp, color = Gray400)
                }
                Switch(checked = checked, onCheckedChange = onChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = MintGreen))
            }
            if (checked && extra != null) {
                extra()
            }
        }
    }
}

@Composable
private fun QuietTimePicker(value: String, onChange: (String) -> Unit) {
    val choices = listOf(
        "20:00", "21:00", "22:00", "23:00",
        "06:00", "07:00", "08:00", "09:00"
    )
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MintBg,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Text(
                value,
                fontSize = 14.sp,
                color = MintGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c) },
                    onClick = {
                        expanded = false
                        onChange(c)
                    }
                )
            }
        }
    }
}
