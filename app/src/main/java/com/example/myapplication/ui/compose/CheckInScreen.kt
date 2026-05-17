package com.example.myapplication.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private val MintGreenDark = Color(0xFF1B8A7D)
private val MintGreenLight = Color(0xFFB2DFDB)
private val GoldBadge = Color(0xFFFFB300)
private val OrangeBadge = Color(0xFFFF6D00)
private val PurpleBadge = Color(0xFF7C4DFF)
private val CardBg = Color(0xFFFFFFFF)

private val HeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF26A69A), Color(0xFF1B8A7D), Color(0xFF15796E))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(onBack: () -> Unit = {}, onNavigateToAchievements: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)
    val scope = rememberCoroutineScope()

    var streak by remember { mutableIntStateOf(0) }
    var todayCheckedIn by remember { mutableStateOf(false) }
    var badges by remember { mutableStateOf<List<BadgeInfo>>(emptyList()) }
    var history by remember { mutableStateOf<List<CheckInRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isCheckingIn by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var newBadges by remember { mutableStateOf<List<BadgeInfo>>(emptyList()) }
    var checkInStreak by remember { mutableIntStateOf(0) }
    var totalCheckIns by remember { mutableIntStateOf(0) }

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
                    totalCheckIns = list.size
                    todayCheckedIn = list.any { it.checkInDate == today.format(DateTimeFormatter.ISO_LOCAL_DATE) }
                }
            } catch (_: Exception) {}

            isLoading = false
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    "打卡成功!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MintGreenDark
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(MintGreen, MintGreenDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✓",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "连续打卡 $checkInStreak 天",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintGreenDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("继续保持，你很棒!", fontSize = 14.sp, color = Color.Gray)

                    if (newBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "解锁新徽章",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldBadge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        newBadges.forEach { badge ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GoldBadge.copy(alpha = 0.08f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(GoldBadge, GoldBadge.copy(alpha = 0.7f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        badge.name.take(1),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(badge.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (badge.description.isNotEmpty()) {
                                        Text(badge.description, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("太棒了!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("控糖打卡", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F7FA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CheckInButtonCard(
                    today = today,
                    todayCheckedIn = todayCheckedIn,
                    isCheckingIn = isCheckingIn,
                    streak = streak,
                    onCheckIn = {
                        isCheckingIn = true
                        scope.launch {
                            try {
                                val resp = withContext(Dispatchers.IO) {
                                    RetrofitClient.getCheckInApiService()
                                        .checkIn(CheckInRequest(userId, 0f, "每日打卡")).execute()
                                }
                                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                    val data = resp.body()?.data
                                    todayCheckedIn = true
                                    checkInStreak = data?.streak ?: (streak + 1)
                                    streak = checkInStreak
                                    newBadges = data?.newBadges ?: emptyList()
                                    showSuccessDialog = true
                                    withContext(Dispatchers.IO) {
                                        val badgesResp = RetrofitClient.getCheckInApiService().getBadges(userId).execute()
                                        if (badgesResp.isSuccessful && badgesResp.body()?.isSuccess == true) {
                                            badges = badgesResp.body()?.data ?: emptyList()
                                        }
                                    }
                                } else {
                                    val msg = resp.body()?.message ?: "打卡失败"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "打卡失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isCheckingIn = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                StatsRow(streak = streak, totalCheckIns = totalCheckIns, badgeCount = badges.count { it.earned })

                Spacer(modifier = Modifier.height(16.dp))

                StreakCalendarCard(today = today, history = history, streak = streak)

                Spacer(modifier = Modifier.height(16.dp))

                BadgesCard(badges = badges)

                Spacer(modifier = Modifier.height(16.dp))

                AchievementEntryCard(onNavigateToAchievements = onNavigateToAchievements)
            }
        }
    }
}

@Composable
private fun CheckInButtonCard(
    today: LocalDate,
    todayCheckedIn: Boolean,
    isCheckingIn: Boolean,
    streak: Int,
    onCheckIn: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderGradient)
                .padding(vertical = 32.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    today.format(DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE")),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (streak > 0) {
                    Text(
                        "已连续打卡 $streak 天",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (todayCheckedIn) {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .shadow(12.dp, CircleShape, ambientColor = Color.White)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "✓",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "今日已打卡",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "明天继续加油哦~",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .scale(if (!isCheckingIn) pulseScale else 1f)
                            .size(120.dp)
                            .shadow(16.dp, CircleShape, ambientColor = Color.White)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onCheckIn,
                            enabled = !isCheckingIn,
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            elevation = null
                        ) {
                            if (isCheckingIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "打卡",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "点击签到",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "记录今日控糖，坚持每一天",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(streak: Int, totalCheckIns: Int, badgeCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatMiniCard(
            value = "$streak",
            label = "连续天数",
            accentColor = OrangeBadge,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            value = "$totalCheckIns",
            label = "累计打卡",
            accentColor = MintGreen,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            value = "$badgeCount",
            label = "获得徽章",
            accentColor = GoldBadge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatMiniCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun StreakCalendarCard(today: LocalDate, history: List<CheckInRecord>, streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("打卡日历", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                Spacer(modifier = Modifier.weight(1f))
                if (streak > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = OrangeBadge.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "连续 $streak 天",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeBadge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val checkedDates = history.map { it.checkInDate }.toSet()
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                val recentDates = (6 downTo 0).map { today.minusDays(it.toLong()) }
                recentDates.forEach { date ->
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val isChecked = checkedDates.contains(dateStr)
                    val isToday = date == today

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            date.format(DateTimeFormatter.ofPattern("E")),
                            fontSize = 11.sp,
                            color = if (isToday) MintGreenDark else Color.Gray,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isToday && !isChecked) Modifier.border(2.dp, MintGreen, CircleShape)
                                    else Modifier
                                )
                                .background(
                                    when {
                                        isChecked -> Brush.verticalGradient(listOf(MintGreen, MintGreenDark))
                                        else -> Brush.verticalGradient(listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE)))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Text(
                                    "✓",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    "${date.dayOfMonth}",
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) MintGreenDark else Color(0xFF999999)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgesCard(badges: List<BadgeInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("我的徽章", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                Spacer(modifier = Modifier.weight(1f))
                val earnedCount = badges.count { it.earned }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GoldBadge.copy(alpha = 0.1f)
                ) {
                    Text(
                        "$earnedCount / ${badges.size}",
                        fontSize = 12.sp,
                        color = GoldBadge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (badges.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("?", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCCCCCC))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("开始打卡，解锁你的第一个徽章", fontSize = 14.sp, color = Color.Gray)
                }
            } else {
                val rows = badges.chunked(3)
                rows.forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeItem(badge = badge, modifier = Modifier.weight(1f))
                        }
                        repeat(3 - rowBadges.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun AchievementEntryCard(onNavigateToAchievements: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToAchievements() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF3E5F5), Color(0xFFEDE7F6), Color(0xFFE8EAF6))
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PurpleBadge.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏅", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("控糖成就", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                Spacer(modifier = Modifier.height(2.dp))
                Text("查看全部成就和挑战进度", fontSize = 12.sp, color = Color(0xFF888888))
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PurpleBadge.copy(alpha = 0.12f)
            ) {
                Text(
                    "查看 →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleBadge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun getBadgeIconRes(badge: BadgeInfo): Int {
    return when {
        badge.name.contains("初心") -> R.drawable.ic_badge_beginner
        badge.name.contains("三日") -> R.drawable.ic_badge_three_days
        badge.name.contains("周达人") -> R.drawable.ic_badge_week_star
        badge.name.contains("半月") -> R.drawable.ic_badge_half_month
        badge.name.contains("月冠军") -> R.drawable.ic_badge_month_champ
        badge.name.contains("百日") -> R.drawable.ic_badge_hundred_days
        badge.name.contains("新星") -> R.drawable.ic_badge_new_star
        badge.name.contains("达人") -> R.drawable.ic_badge_expert
        badge.name.contains("精英") -> R.drawable.ic_badge_elite
        badge.name.contains("大师") -> R.drawable.ic_badge_master
        badge.name.contains("之王") -> R.drawable.ic_badge_king
        badge.name.contains("先锋") -> R.drawable.ic_badge_pioneer
        badge.name.contains("完美") -> R.drawable.ic_badge_perfect
        badge.name.contains("守护") -> R.drawable.ic_badge_guardian
        badge.name.contains("至尊") -> R.drawable.ic_badge_supreme
        else -> R.drawable.ic_badge_beginner
    }
}

@Composable
private fun BadgeItem(badge: BadgeInfo, modifier: Modifier = Modifier) {
    val tint = if (badge.earned) {
        when (badge.category) {
            "streak" -> OrangeBadge
            "total" -> GoldBadge
            "special" -> PurpleBadge
            else -> MintGreen
        }
    } else Color(0xFFBDBDBD)

    val iconRes = getBadgeIconRes(badge)

    Column(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .then(
                    if (badge.earned) Modifier.shadow(6.dp, CircleShape, ambientColor = tint)
                    else Modifier
                )
                .clip(CircleShape)
                .background(
                    if (badge.earned)
                        Brush.verticalGradient(listOf(tint.copy(alpha = 0.25f), tint.copy(alpha = 0.08f)))
                    else
                        Brush.verticalGradient(listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE)))
                )
                .then(
                    if (badge.earned) Modifier.border(2.dp, tint.copy(alpha = 0.35f), CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = badge.name,
                modifier = Modifier.size(36.dp),
                alpha = if (badge.earned) 1f else 0.4f
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            badge.name,
            fontSize = 11.sp,
            fontWeight = if (badge.earned) FontWeight.Bold else FontWeight.Normal,
            color = if (badge.earned) Color(0xFF333333) else Color(0xFFAAAAAA),
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        if (!badge.earned && badge.progress > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE8E8E8))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(badge.progress / 100f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(listOf(MintGreenLight, MintGreen))
                        )
                )
            }
            Text(
                "${badge.progress}%",
                fontSize = 9.sp,
                color = Color(0xFFAAAAAA)
            )
        }
    }
}
