package com.example.myapplication.ui.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val FamilyHeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF26A69A), Color(0xFF1B8A7D), Color(0xFF15796E))
)
private val DangerColor = Color(0xFFEF5350)
private val WarningColor = Color(0xFFFFA726)
private val InfoColor = Color(0xFF42A5F5)

private val defaultFamilyAvatars = listOf(
    "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=400&h=400&fit=crop",
    "https://images.unsplash.com/photo-1511895426328-dc8714191300?w=400&h=400&fit=crop",
    "https://images.unsplash.com/photo-1606567595334-d39972c85dbe?w=400&h=400&fit=crop",
    "https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=400&fit=crop",
    "https://images.unsplash.com/photo-1545558014-8692077e9b5c?w=400&h=400&fit=crop",
    "https://images.unsplash.com/photo-1484723091739-30a097e8f929?w=400&h=400&fit=crop"
)

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
    var showFamilyPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showMemberDetail by remember { mutableStateOf<FamilyMemberInfo?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
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
                    selectedFamily?.let { sel ->
                        val updated = families.find { it.id == sel.id }
                        if (updated != null) selectedFamily = updated
                        else selectedFamily = families.firstOrNull()
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
            if (selectedFamily == null) { members = emptyList(); alerts = emptyList() }
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

    if (showFamilyPicker) {
        FamilyPickerDialog(
            families = families,
            currentFamily = selectedFamily,
            onSelect = { family ->
                selectedFamily = family
                showFamilyPicker = false
                refreshTrigger++
            },
            onDismiss = { showFamilyPicker = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除", fontWeight = FontWeight.Bold) },
            text = { Text("删除家庭「${selectedFamily?.name}」后无法恢复，所有成员将被移除。确定要删除吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFamily?.let { family ->
                            Thread {
                                try {
                                    val resp = RetrofitClient.getFamilyApiService().deleteFamily(family.id, userId).execute()
                                    if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                        selectedFamily = null
                                        refreshTrigger++
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            Toast.makeText(context, "家庭已删除", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            Toast.makeText(context, resp.body()?.message ?: "删除失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        Toast.makeText(context, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }.start()
                        }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("删除", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消", color = Color.Gray) } },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showEditDialog) {
        selectedFamily?.let { family ->
            EditFamilyDialog(
                family = family,
                onDismiss = { showEditDialog = false },
                onSave = { name, desc ->
                    Thread {
                        try {
                            val resp = RetrofitClient.getFamilyApiService()
                                .updateFamily(family.id, userId, UpdateFamilyRequest(name = name, description = desc)).execute()
                            if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                selectedFamily = resp.body()?.data
                                refreshTrigger++
                            }
                        } catch (_: Exception) {}
                    }.start()
                    showEditDialog = false
                }
            )
        }
    }

    if (showAvatarPicker) {
        AvatarPickerDialog(
            onSelect = { url ->
                selectedFamily?.let { family ->
                    Thread {
                        try {
                            val resp = RetrofitClient.getFamilyApiService()
                                .updateFamily(family.id, userId, UpdateFamilyRequest(avatarUrl = url)).execute()
                            if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                selectedFamily = resp.body()?.data
                                refreshTrigger++
                            }
                        } catch (_: Exception) {}
                    }.start()
                }
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false }
        )
    }

    showMemberDetail?.let { member ->
        MemberDetailDialog(member = member, onDismiss = { showMemberDetail = null })
    }

    val isOwner = members.any { it.userId == userId && it.role == "owner" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家庭共管", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                },
                actions = {
                    if (families.isNotEmpty()) {
                        TextButton(onClick = { showFamilyPicker = true }) {
                            Text("切换", color = MintGreen, fontWeight = FontWeight.Bold)
                        }
                    }
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
            EmptyFamilyView(
                modifier = Modifier.fillMaxSize().padding(padding),
                onCreateFamily = { showCreateDialog = true },
                onJoinFamily = { showJoinDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F7FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                selectedFamily?.let { family ->
                    item {
                        FamilyHeaderCard(
                            family = family,
                            memberCount = members.size,
                            context = context,
                            isOwner = isOwner,
                            onEditAvatar = { showAvatarPicker = true },
                            onEdit = { showEditDialog = true },
                            onDelete = { showDeleteConfirm = true }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.clickable { showCreateDialog = true },
                                shape = RoundedCornerShape(10.dp), color = MintGreen.copy(alpha = 0.1f)
                            ) {
                                Text("+ 新建家庭", fontSize = 13.sp, color = MintGreen, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                            }
                            Surface(
                                modifier = Modifier.clickable { showJoinDialog = true },
                                shape = RoundedCornerShape(10.dp), color = MintGreen.copy(alpha = 0.1f)
                            ) {
                                Text("+ 加入家庭", fontSize = 13.sp, color = MintGreen, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                            }
                        }
                    }

                    item {
                        Text("家庭成员 (${members.size})", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333), modifier = Modifier.padding(vertical = 4.dp))
                    }

                    items(members) { member ->
                        FamilyMemberCard(
                            member = member, currentUserId = userId, isOwner = isOwner,
                            onClick = { showMemberDetail = member },
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
                            Text("智能预警", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = CircleShape, color = DangerColor) {
                                    Text("$unreadCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                Thread {
                                    try { RetrofitClient.getFamilyApiService().checkAlerts(family.id).execute(); refreshTrigger++ }
                                    catch (_: Exception) {}
                                }.start()
                            }) { Text("刷新检查", color = MintGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                        }
                    }

                    if (alerts.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                                    Text("暂无预警信息，一切正常", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(alerts.take(10)) { alert ->
                            AlertCard(alert = alert, onMarkRead = {
                                Thread {
                                    try { RetrofitClient.getFamilyApiService().markAlertRead(alert.id).execute(); refreshTrigger++ }
                                    catch (_: Exception) {}
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
private fun EmptyFamilyView(modifier: Modifier, onCreateFamily: () -> Unit, onJoinFamily: () -> Unit) {
    Box(modifier = modifier.background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
            Box(modifier = Modifier.size(100.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(MintGreen.copy(alpha = 0.15f), MintGreen.copy(alpha = 0.05f)))),
                contentAlignment = Alignment.Center) {
                Text("家", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = MintGreen.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("还没有加入家庭", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Spacer(modifier = Modifier.height(6.dp))
            Text("创建或加入家庭，和家人一起控糖", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onCreateFamily, colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("创建家庭", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onJoinFamily, shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MintGreen),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(MintGreen, MintGreen)))) {
                Text("加入家庭", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AvatarImage(avatarUrl: String?, name: String, size: Int = 48, modifier: Modifier = Modifier) {
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
            contentDescription = name,
            modifier = modifier.size(size.dp).clip(CircleShape).border(1.dp, Color(0xFFE0E0E0), CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier.size(size.dp).clip(CircleShape)
            .background(Brush.verticalGradient(listOf(MintGreen.copy(alpha = 0.2f), MintGreen.copy(alpha = 0.08f)))),
            contentAlignment = Alignment.Center) {
            Text((name.firstOrNull() ?: 'U').uppercase(), fontWeight = FontWeight.Bold, color = MintGreen, fontSize = (size / 2.4).sp)
        }
    }
}

@Composable
private fun FamilyHeaderCard(
    family: FamilyGroupInfo, memberCount: Int, context: Context, isOwner: Boolean,
    onEditAvatar: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = isOwner, onClick = onEditAvatar)) {
                if (!family.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(family.avatarUrl).crossfade(true).build(),
                        contentDescription = family.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(FamilyHeaderGradient), contentAlignment = Alignment.Center) {
                        Text(family.name.take(1), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.3f))
                    }
                }
                if (isOwner) {
                    Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.4f)) {
                        Text("更换封面", fontSize = 11.sp, color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                color = Color.White, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(family.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                            if (!family.description.isNullOrBlank()) {
                                Text(family.description!!, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                            Text("${memberCount}位家庭成员", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        }
                        if (isOwner) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(modifier = Modifier.clickable(onClick = onEdit), shape = RoundedCornerShape(8.dp),
                                    color = MintGreen.copy(alpha = 0.1f)) {
                                    Text("编辑", fontSize = 12.sp, color = MintGreen, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                }
                                Surface(modifier = Modifier.clickable(onClick = onDelete), shape = RoundedCornerShape(8.dp),
                                    color = DangerColor.copy(alpha = 0.08f)) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "删除家庭",
                                        tint = DangerColor.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(6.dp).size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF5F7FA)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("邀请码", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(family.inviteCode, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF222222), letterSpacing = 4.sp)
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("邀请码", family.inviteCode))
                                    Toast.makeText(context, "邀请码已复制: ${family.inviteCode}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) { Text("复制", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberCard(
    member: FamilyMemberInfo, currentUserId: Long, isOwner: Boolean, onClick: () -> Unit, onRemove: () -> Unit
) {
    val sugarRatio = if ((member.sugarLimit ?: 25f) > 0) (member.todaySugar ?: 0f) / (member.sugarLimit ?: 25f) else 0f
    val statusColor = when { sugarRatio > 1f -> DangerColor; sugarRatio > 0.8f -> WarningColor; else -> MintGreen }
    val statusText = when { sugarRatio > 1f -> "超标"; sugarRatio > 0.8f -> "接近"; else -> "正常" }

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarImage(avatarUrl = member.avatarUrl, name = member.username, size = 48)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.nickname ?: member.username, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF333333))
                    if (member.role == "owner") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFF3E0)) {
                            Text("管理员", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("糖摄入 ", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format("%.1f", member.todaySugar ?: 0f)}/${String.format("%.0f", member.sugarLimit ?: 25f)}g",
                        fontSize = 12.sp, fontWeight = FontWeight.Medium, color = statusColor)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("连续${member.streak ?: 0}天", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                if (isOwner && member.userId != currentUserId) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF5F5F5),
                        modifier = Modifier.clickable(onClick = onRemove)) {
                        Text("移除", fontSize = 10.sp, color = Color(0xFFBDBDBD),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberDetailDialog(member: FamilyMemberInfo, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarImage(avatarUrl = member.avatarUrl, name = member.username, size = 44)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(member.nickname ?: member.username, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (member.role == "owner") Text("管理员", fontSize = 12.sp, color = Color(0xFFE65100))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                DetailRow("今日糖摄入", "${String.format("%.1f", member.todaySugar ?: 0f)}g",
                    "/ ${String.format("%.0f", member.sugarLimit ?: 25f)}g",
                    if ((member.todaySugar ?: 0f) > (member.sugarLimit ?: 25f)) DangerColor else MintGreen)
                DetailRow("连续打卡", "${member.streak ?: 0}天", color = Color(0xFFFF6D00))
                DetailRow("上次打卡", member.lastCheckIn ?: "暂无记录", color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                val sugarRatio = if ((member.sugarLimit ?: 25f) > 0)
                    ((member.todaySugar ?: 0f) / (member.sugarLimit ?: 25f)).coerceIn(0f, 1.5f) else 0f
                Text("今日糖分进度", fontSize = 12.sp, color = Color.Gray)
                LinearProgressIndicator(
                    progress = (sugarRatio / 1.5f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = when { sugarRatio > 1f -> DangerColor; sugarRatio > 0.8f -> WarningColor; else -> MintGreen },
                    trackColor = Color(0xFFF0F0F0))
                Text("${String.format("%.0f", sugarRatio * 100)}% 额度使用", fontSize = 11.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                shape = RoundedCornerShape(10.dp)) { Text("关闭", fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun DetailRow(label: String, value: String, limit: String = "", color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            if (limit.isNotEmpty()) Text(limit, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
        }
    }
}

@Composable
private fun AlertCard(alert: HealthAlertInfo, onMarkRead: () -> Unit) {
    val (severityLabel, color) = when (alert.severity) {
        "danger" -> "危险" to DangerColor; "warning" -> "警告" to WarningColor; else -> "提示" to InfoColor
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (alert.isRead) Color.White else color.copy(alpha = 0.04f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(if (alert.isRead) Color(0xFFE0E0E0) else color))
            Row(modifier = Modifier.weight(1f).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f)) {
                            Text(severityLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(alert.createdAt?.take(16) ?: "", fontSize = 11.sp, color = Color(0xFFBDBDBD))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(alert.message, fontSize = 14.sp, color = if (alert.isRead) Color.Gray else Color(0xFF333333), lineHeight = 20.sp)
                }
                if (!alert.isRead) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5),
                        modifier = Modifier.clickable(onClick = onMarkRead)) {
                        Text("已读", fontSize = 12.sp, color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyPickerDialog(
    families: List<FamilyGroupInfo>, currentFamily: FamilyGroupInfo?,
    onSelect: (FamilyGroupInfo) -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择家庭", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                families.forEach { family ->
                    val isSelected = family.id == currentFamily?.id
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(family) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MintGreen.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(MintGreen, MintGreen))) else null
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(if (isSelected) MintGreen else Color(0xFFCCCCCC)),
                                contentAlignment = Alignment.Center) {
                                Text(family.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(family.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                                    color = if (isSelected) MintGreen else Color(0xFF333333))
                                Text("${family.memberCount}位成员", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (isSelected) Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭", color = Color.Gray) } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun EditFamilyDialog(family: FamilyGroupInfo, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf(family.name) }
    var desc by remember { mutableStateOf(family.description ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑家庭信息", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("家庭名称") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen))
                OutlinedTextField(value = desc, onValueChange = { desc = it.take(50) }, label = { Text("个性签名") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("${desc.length}/50", fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen))
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name, desc) }, enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen), shape = RoundedCornerShape(10.dp)) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun AvatarPickerDialog(onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择家庭封面", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                defaultFamilyAvatars.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { url ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                                contentDescription = "封面选项",
                                modifier = Modifier.weight(1f).aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelect(url) },
                                contentScale = ContentScale.Crop
                            )
                        }
                        repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CreateFamilyDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建家庭", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("家庭名称") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen))
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onCreate(name) }, enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen), shape = RoundedCornerShape(10.dp)) {
                Text("创建", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun JoinFamilyDialog(onDismiss: () -> Unit, onJoin: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("加入家庭", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            OutlinedTextField(value = code, onValueChange = { code = it.uppercase().take(6) },
                label = { Text("邀请码") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen))
        },
        confirmButton = {
            Button(onClick = { if (code.length == 6) onJoin(code) }, enabled = code.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen), shape = RoundedCornerShape(10.dp)) {
                Text("加入", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) } },
        shape = RoundedCornerShape(24.dp)
    )
}
