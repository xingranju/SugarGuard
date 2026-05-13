package com.example.myapplication.ui.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var families by remember { mutableStateOf<List<FamilyGroupInfo>>(emptyList()) }
    var selectedFamily by remember { mutableStateOf<FamilyGroupInfo?>(null) }
    var members by remember { mutableStateOf<List<FamilyMemberInfo>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<HealthAlertInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.getFamilyApiService().getMyFamilies(userId).execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    families = resp.body()?.data ?: emptyList()
                    if (selectedFamily == null && families.isNotEmpty()) {
                        selectedFamily = families.first()
                    }
                }
            } catch (_: Exception) {}

            selectedFamily?.let { family ->
                try {
                    val mResp = RetrofitClient.getFamilyApiService().getFamilyMembers(family.id).execute()
                    if (mResp.isSuccessful && mResp.body()?.isSuccess == true) {
                        members = mResp.body()?.data ?: emptyList()
                    }
                } catch (_: Exception) {}
                try {
                    val aResp = RetrofitClient.getFamilyApiService().getAlerts(family.id).execute()
                    if (aResp.isSuccessful && aResp.body()?.isSuccess == true) {
                        alerts = aResp.body()?.data ?: emptyList()
                    }
                } catch (_: Exception) {}
            }
            isLoading = false
        }
    }

    if (showCreateDialog) {
        CreateFamilyDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                Thread {
                    try {
                        val resp = RetrofitClient.getFamilyApiService()
                            .createFamily(userId, CreateFamilyRequest(name)).execute()
                        if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                            selectedFamily = resp.body()?.data
                            refreshTrigger++
                        }
                    } catch (_: Exception) {}
                }.start()
                showCreateDialog = false
            }
        )
    }

    if (showJoinDialog) {
        JoinFamilyDialog(
            onDismiss = { showJoinDialog = false },
            onJoin = { code ->
                Thread {
                    try {
                        val resp = RetrofitClient.getFamilyApiService()
                            .joinFamily(userId, JoinFamilyRequest(code)).execute()
                        if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                            selectedFamily = resp.body()?.data
                            refreshTrigger++
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                Toast.makeText(context, "加入成功", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                Toast.makeText(context, resp.body()?.message ?: "加入失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Toast.makeText(context, "加入失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
                showJoinDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家庭共管") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else if (families.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(MintGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = MintGreen, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("还没有加入家庭", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("创建或加入家庭，一起控糖", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(200.dp)
                    ) { Text("创建家庭", fontSize = 16.sp) }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showJoinDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(200.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MintGreen)
                    ) { Text("加入家庭", fontSize = 16.sp) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F7FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                selectedFamily?.let { family ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = MintGreen, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(family.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("${family.memberCount}人", fontSize = 14.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("邀请码: ", fontSize = 13.sp, color = Color.Gray)
                                    Text(family.inviteCode, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("邀请码", family.inviteCode))
                                            Toast.makeText(context, "邀请码已复制", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("家庭成员", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                    }

                    items(members) { member ->
                        FamilyMemberCard(member = member, currentUserId = userId, isOwner = members.any { it.userId == userId && it.role == "owner" },
                            onRemove = {
                                Thread {
                                    try {
                                        RetrofitClient.getFamilyApiService()
                                            .removeMember(family.id, member.userId, userId).execute()
                                        refreshTrigger++
                                    } catch (_: Exception) {}
                                }.start()
                            }
                        )
                    }

                    val unreadCount = alerts.count { !it.isRead }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("智能预警", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge(containerColor = Color(0xFFEF5350)) {
                                    Text("$unreadCount", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                Thread {
                                    try {
                                        RetrofitClient.getFamilyApiService().checkAlerts(family.id).execute()
                                        refreshTrigger++
                                    } catch (_: Exception) {}
                                }.start()
                            }) { Text("刷新检查", color = MintGreen, fontSize = 13.sp) }
                        }
                    }

                    if (alerts.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("暂无预警信息，一切正常", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(alerts.take(10)) { alert ->
                            AlertCard(alert = alert, onMarkRead = {
                                Thread {
                                    try {
                                        RetrofitClient.getFamilyApiService().markAlertRead(alert.id).execute()
                                        refreshTrigger++
                                    } catch (_: Exception) {}
                                }.start()
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberCard(member: FamilyMemberInfo, currentUserId: Long, isOwner: Boolean, onRemove: () -> Unit) {
    val sugarRatio = if ((member.sugarLimit ?: 25f) > 0) (member.todaySugar ?: 0f) / (member.sugarLimit ?: 25f) else 0f
    val statusColor = when {
        sugarRatio > 1f -> Color(0xFFEF5350)
        sugarRatio > 0.8f -> Color(0xFFFFA726)
        else -> MintGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(MintGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text((member.username.firstOrNull() ?: 'U').uppercase(), fontWeight = FontWeight.Bold, color = MintGreen, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.nickname ?: member.username, fontWeight = FontWeight.Medium)
                    if (member.role == "owner") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFF3E0)) {
                            Text("管理员", fontSize = 10.sp, color = Color(0xFFE65100), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("糖摄入 ", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format("%.1f", member.todaySugar ?: 0f)}/${String.format("%.0f", member.sugarLimit ?: 25f)}g",
                        fontSize = 12.sp, fontWeight = FontWeight.Medium, color = statusColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("连续${member.streak ?: 0}天", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))

            if (isOwner && member.userId != currentUserId) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "移除", tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: HealthAlertInfo, onMarkRead: () -> Unit) {
    val (icon, color) = when (alert.severity) {
        "danger" -> Icons.Default.Error to Color(0xFFEF5350)
        "warning" -> Icons.Default.Warning to Color(0xFFFFA726)
        else -> Icons.Default.Info to Color(0xFF42A5F5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (alert.isRead) Color.White else color.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alert.message, fontSize = 14.sp, color = if (alert.isRead) Color.Gray else Color.Black)
                Text(alert.createdAt?.take(16) ?: "", fontSize = 11.sp, color = Color(0xFFBDBDBD))
            }
            if (!alert.isRead) {
                TextButton(onClick = onMarkRead, modifier = Modifier.height(28.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                    Text("已读", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun CreateFamilyDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建家庭", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("家庭名称") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen)
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name) }, enabled = name.isNotBlank()) {
                Text("创建", color = if (name.isNotBlank()) MintGreen else Color.Gray)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) } }
    )
}

@Composable
private fun JoinFamilyDialog(onDismiss: () -> Unit, onJoin: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("加入家庭", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = code, onValueChange = { code = it.uppercase().take(6) },
                label = { Text("邀请码") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen)
            )
        },
        confirmButton = {
            TextButton(onClick = { if (code.length == 6) onJoin(code) }, enabled = code.length == 6) {
                Text("加入", color = if (code.length == 6) MintGreen else Color.Gray)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) } }
    )
}
