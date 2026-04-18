package com.example.myapplication.ui.compose

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    var showLogoutDialog by remember { mutableStateOf(false) }
    var dataEncryption by remember { mutableStateOf(settingsPrefs.getBoolean("data_encryption", true)) }
    var anonymousShare by remember { mutableStateOf(settingsPrefs.getBoolean("anonymous_share", false)) }
    var riceVisual by remember { mutableStateOf(settingsPrefs.getBoolean("rice_visual", true)) }
    var darkMode by remember { mutableStateOf(settingsPrefs.getBoolean("dark_mode", false)) }
    var sugarUnit by remember { mutableStateOf(settingsPrefs.getString("sugar_unit", "g") ?: "g") }
    var cacheSize by remember { mutableStateOf("${(context.cacheDir.walkTopDown().sumOf { it.length() } / 1024.0 / 1024.0).let { String.format("%.1f", it) }}MB") }
    var showAgreement by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showReminderTime by remember { mutableStateOf(false) }

    fun saveSetting(key: String, value: Boolean) {
        settingsPrefs.edit().putBoolean(key, value).apply()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Color(0xFF757575))
            }
            Text("设置", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup("隐私与安全") {
                SettingsToggleRow("数据加密存储", dataEncryption) { dataEncryption = it; saveSetting("data_encryption", it) }
                SettingsDivider()
                SettingsToggleRow("匿名数据共享", anonymousShare) { anonymousShare = it; saveSetting("anonymous_share", it) }
                SettingsDivider()
                SettingsClickRow("清除缓存数据", cacheSize) {
                    context.cacheDir.deleteRecursively()
                    context.cacheDir.mkdirs()
                    cacheSize = "0.0MB"
                    android.widget.Toast.makeText(context, "缓存已清除", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            SettingsGroup("显示与偏好") {
                SettingsClickRow("糖分单位", if (sugarUnit == "g") "克(g)" else "方糖") {
                    sugarUnit = if (sugarUnit == "g") "cube" else "g"
                    settingsPrefs.edit().putString("sugar_unit", sugarUnit).apply()
                }
                SettingsDivider()
                SettingsToggleRow("米饭类比可视化", riceVisual) { riceVisual = it; saveSetting("rice_visual", it) }
                SettingsDivider()
                SettingsToggleRow("深色模式", darkMode) { darkMode = it; saveSetting("dark_mode", it) }
            }

            SettingsGroup("关于") {
                SettingsValueRow("版本", "v3.5.1")
                SettingsDivider()
                SettingsClickRow("用户协议", "") { showAgreement = true }
                SettingsDivider()
                SettingsClickRow("隐私政策", "") { showPrivacy = true }
            }

            // Logout button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF2F2)
            ) {
                Text(
                    "退出登录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF5350),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showReminderTime) {
        AlertDialog(
            onDismissRequest = { showReminderTime = false },
            title = { Text("提醒时段设置", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("当前提醒时段：8:00 - 22:00", fontSize = 14.sp)
                    Text("我们会在此时段内发送控糖提醒和记录提醒，不会在深夜打扰您。", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            },
            confirmButton = { TextButton(onClick = { showReminderTime = false }) { Text("确定") } }
        )
    }

    if (showAgreement) {
        AlertDialog(
            onDismissRequest = { showAgreement = false },
            title = { Text("用户协议", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("糖知APP用户服务协议\n\n欢迎使用「糖知」应用。使用本应用前，请仔细阅读以下条款：\n\n1. 服务说明\n糖知是一款帮助用户管理日常糖分摄入的健康管理工具。本应用提供食物识别、糖分记录、健康分析等功能。\n\n2. 用户责任\n用户应确保提供的健康数据真实准确。本应用的建议仅供参考，不构成医疗建议。\n\n3. 隐私保护\n我们重视用户隐私，详见隐私政策。\n\n4. 免责声明\n本应用提供的营养数据和AI分析仅供参考。如有健康问题，请咨询专业医生。\n\n5. 知识产权\n本应用及相关内容的知识产权归开发团队所有。",
                        fontSize = 12.sp, color = Color(0xFF555555), lineHeight = 20.sp)
                }
            },
            confirmButton = { TextButton(onClick = { showAgreement = false }) { Text("确定") } }
        )
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("隐私政策", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("糖知APP隐私政策\n\n最后更新: 2026年3月\n\n1. 信息收集\n我们收集您主动提供的健康数据（身高、体重、饮食记录等）以提供服务。\n\n2. 信息使用\n收集的数据仅用于提供个性化健康分析和建议。\n\n3. 数据存储\n您的数据加密存储在本地设备中。开启「匿名数据共享」后，脱敏数据可能用于改进AI模型。\n\n4. 数据安全\n我们采用AES-256加密技术保护您的数据安全。\n\n5. 用户权利\n您有权随时删除个人数据、导出数据、或撤销数据使用授权。\n\n6. 联系方式\n如有隐私相关问题，请联系：privacy@tangzhi.app",
                        fontSize = 12.sp, color = Color(0xFF555555), lineHeight = 20.sp)
                }
            },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("确定") } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出", fontWeight = FontWeight.Bold) },
            text = { Text("确定要退出登录吗？退出后需要重新引导注册。") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
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

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFAFAFA),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Text(
                    title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF333333))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MintGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@Composable
private fun SettingsValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF333333))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 14.sp, color = Color(0xFFBDBDBD))
        }
    }
}

@Composable
private fun SettingsClickRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF333333))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (value.isNotEmpty()) {
                Text(value, fontSize = 14.sp, color = Color(0xFFBDBDBD))
            }
            Text("›", fontSize = 16.sp, color = Color(0xFFD0D0D0))
        }
    }
}

@Composable
private fun SettingsDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFF5F5F5)
    )
}

