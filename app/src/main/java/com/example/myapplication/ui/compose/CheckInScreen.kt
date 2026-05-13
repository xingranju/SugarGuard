package com.example.myapplication.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var streak by remember { mutableIntStateOf(0) }
    var todayCheckedIn by remember { mutableStateOf(false) }
    var badges by remember { mutableStateOf<List<BadgeInfo>>(emptyList()) }
    var history by remember { mutableStateOf<List<CheckInRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var newBadges by remember { mutableStateOf<List<BadgeInfo>>(emptyList()) }
    var checkInStreak by remember { mutableIntStateOf(0) }

    val today = LocalDate.now()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val streakResp = RetrofitClient.getCheckInApiService().getStreak(userId).execute()
                if (streakResp.isSuccessful && streakResp.body()?.isSuccess == true) {
                    streak = (streakResp.body()?.data as? Number)?.toInt() ?: 0
                }
            } catch (_: Exception) {}
            try {
                val badgesResp = RetrofitClient.getCheckInApiService().getBadges(userId).execute()
                if (badgesResp.isSuccessful && badgesResp.body()?.isSuccess == true) {
                    badges = badgesResp.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {}
            try {
                val historyResp = RetrofitClient.getCheckInApiService().getHistory(userId).execute()
                if (historyResp.isSuccessful && historyResp.body()?.isSuccess == true) {
                    val list = historyResp.body()?.data ?: emptyList()
                    history = list
                    todayCheckedIn = list.any { it.checkInDate == today.format(DateTimeFormatter.ISO_LOCAL_DATE) }
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("打卡成功", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("连续打卡 $checkInStreak 天", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                    if (newBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("恭喜获得新徽章:", fontSize = 14.sp, color = Color.Gray)
                        newBadges.forEach { badge ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(getBadgeIcon(badge.category), contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(badge.name, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) { Text("太棒了", color = MintGreen) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("控糖打卡") },
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
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F7FA)).verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(today.format(DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE")), fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (todayCheckedIn) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("今日已打卡", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        } else {
                            Button(
                                onClick = {
                                    Thread {
                                        try {
                                            val resp = RetrofitClient.getCheckInApiService()
                                                .checkIn(CheckInRequest(userId, 0f, "每日打卡")).execute()
                                            if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                                val data = resp.body()?.data
                                                todayCheckedIn = true
                                                checkInStreak = data?.streak ?: (streak + 1)
                                                streak = checkInStreak
                                                newBadges = data?.newBadges ?: emptyList()
                                                showSuccessDialog = true
                                                val badgesResp = RetrofitClient.getCheckInApiService().getBadges(userId).execute()
                                                if (badgesResp.isSuccessful && badgesResp.body()?.isSuccess == true) {
                                                    badges = badgesResp.body()?.data ?: emptyList()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                Toast.makeText(context, "打卡失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }.start()
                                },
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                            ) {
                                Text("打卡", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("连续打卡", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$streak", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        Text("天", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            val recentDates = (6 downTo 0).map { today.minusDays(it.toLong()) }
                            val checkedDates = history.map { it.checkInDate }.toSet()
                            recentDates.forEach { date ->
                                val isChecked = checkedDates.contains(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(date.format(DateTimeFormatter.ofPattern("E")), fontSize = 10.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.size(28.dp).clip(CircleShape)
                                            .background(if (isChecked) MintGreen else Color(0xFFE0E0E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text("${date.dayOfMonth}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("我的徽章", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (badges.isEmpty()) {
                            Text("开始打卡，解锁你的第一个徽章", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        } else {
                            val rows = badges.chunked(3)
                            rows.forEach { rowBadges ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    rowBadges.forEach { badge ->
                                        BadgeItem(badge = badge, modifier = Modifier.weight(1f))
                                    }
                                    repeat(3 - rowBadges.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: BadgeInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = getBadgeIcon(badge.category)
        val tint = if (badge.earned) {
            when (badge.category) {
                "streak" -> Color(0xFFFF6D00)
                "total" -> Color(0xFFFFB300)
                "special" -> Color(0xFF7C4DFF)
                else -> MintGreen
            }
        } else Color(0xFFBDBDBD)

        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape)
                .background(if (badge.earned) tint.copy(alpha = 0.15f) else Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = badge.name, tint = tint, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(badge.name, fontSize = 11.sp, fontWeight = if (badge.earned) FontWeight.Bold else FontWeight.Normal,
            color = if (badge.earned) Color.Black else Color.Gray, textAlign = TextAlign.Center, maxLines = 1)

        if (!badge.earned && badge.progress > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = badge.progress / 100f,
                modifier = Modifier.width(40.dp).height(3.dp),
                color = MintGreen,
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

private fun getBadgeIcon(category: String): ImageVector {
    return when (category) {
        "streak" -> Icons.Default.LocalFireDepartment
        "total" -> Icons.Default.EmojiEvents
        "special" -> Icons.Default.Star
        else -> Icons.Default.Verified
    }
}
