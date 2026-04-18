package com.example.myapplication.ui.compose

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private enum class AchievementTier(val label: String, val bgColors: List<Color>, val badgeColor: Color) {
    BRONZE("入门", listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)), Color(0xFFE65100)),
    SILVER("进阶", listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC)), Color(0xFF546E7A)),
    GOLD("精英", listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)), Color(0xFFF9A825)),
    DIAMOND("大师", listOf(Color(0xFFE8EAF6), Color(0xFFC5CAE9)), Color(0xFF283593)),
    LEGENDARY("传说", listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0)), Color(0xFFAD1457))
}

private data class AchievementItem(
    val icon: String,
    val title: String,
    val description: String,
    val date: String,
    val unlocked: Boolean,
    val progress: String? = null,
    val tier: AchievementTier = AchievementTier.BRONZE,
    val category: String = ""
)

@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")

    var consecutiveDays by remember { mutableIntStateOf(0) }
    var totalRecords by remember { mutableIntStateOf(0) }
    var totalScanDays by remember { mutableIntStateOf(0) }
    var firstRecordDate by remember { mutableStateOf("") }
    var weekCompleted by remember { mutableStateOf(listOf(false, false, false, false, false, false, false)) }
    var bestDaySugar by remember { mutableStateOf(Float.MAX_VALUE) }
    var zeroCaffeineDays by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val mealApi = RetrofitClient.getMealApiService()
                    val profileApi = RetrofitClient.getUserProfileApiService()
                    val healthApi = RetrofitClient.getDailyHealthRecordApiService()
                    @Suppress("UNUSED_VARIABLE")
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    val profileResp = profileApi.getHealthProfile().execute()
                    val sugarLimit = if (profileResp.isSuccessful && profileResp.body()?.isSuccess == true)
                        profileResp.body()?.data?.sugarLimit ?: 25f else 25f

                    val countResp = healthApi.getRecordCount().execute()
                    totalScanDays = if (countResp.isSuccessful && countResp.body()?.isSuccess == true)
                        (countResp.body()?.data ?: 0).toInt() else 0

                    fun getDaySugar(dateStr: String): Float {
                        try {
                            val r = mealApi.getDailyMeals(userId.toInt(), dateStr).execute()
                            if (r.isSuccessful && r.body()?.isSuccess == true) {
                                val data = r.body()?.data ?: return 0f
                                @Suppress("UNCHECKED_CAST")
                                val meals = data["meals"] as? List<Map<String, Any?>> ?: return 0f
                                return meals.sumOf { (it["sugarContent"] as? Number ?: it["sugar_content"] as? Number)?.toDouble() ?: 0.0 }.toFloat()
                            }
                        } catch (_: Exception) {}
                        return 0f
                    }

                    totalRecords = totalScanDays * 4

                    val today = LocalDate.now()
                    firstRecordDate = today.minusDays(totalScanDays.toLong().coerceAtLeast(1) - 1)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                    var consecutive = 0
                    var minSugar = Float.MAX_VALUE
                    for (i in 0..180) {
                        val checkDate = today.minusDays(i.toLong())
                        val dateStr = checkDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val sugar = getDaySugar(dateStr)
                        if (sugar > 0 && sugar < minSugar) minSugar = sugar
                        if (i <= 30) {
                            if (sugar > 0 && sugar <= sugarLimit) {
                                consecutive++
                            } else if (i == 0 && sugar == 0f) {
                                continue
                            } else if (consecutive > 0 || i > 0) {
                                if (consecutive == 0 && i <= 1) continue
                                else if (i <= 30 && consecutive > 0) break
                            }
                        }
                    }
                    consecutiveDays = consecutive
                    bestDaySugar = if (minSugar == Float.MAX_VALUE) 0f else minSugar

                    val weekStatus = mutableListOf<Boolean>()
                    for (i in 6 downTo 0) {
                        val date = today.minusDays(i.toLong())
                        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val sugar = getDaySugar(dateStr)
                        weekStatus.add(sugar > 0 && sugar <= sugarLimit)
                    }
                    weekCompleted = weekStatus
                } catch (_: Exception) {}
            }
        }
    }

    val allAchievements = remember(totalRecords, consecutiveDays, totalScanDays, bestDaySugar) {
        buildAchievementList(totalRecords, consecutiveDays, totalScanDays, bestDaySugar, firstRecordDate)
    }

    val unlockedAchievements = allAchievements.filter { it.unlocked }
    val lockedAchievements = allAchievements.filter { !it.unlocked }
    val totalCount = allAchievements.size
    val unlockedCount = unlockedAchievements.size

    Column(
        modifier = Modifier.fillMaxSize().background(Gray50)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回", tint = Gray600) }
            Text("控糖成就", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("连续达标", fontSize = 12.sp, color = Gray400, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$consecutiveDays", fontSize = 56.sp, fontWeight = FontWeight.Black, color = MintGreen)
                        Text("天", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray400,
                            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        weekDays.forEachIndexed { index, day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    modifier = Modifier.size(32.dp), shape = CircleShape,
                                    color = if (weekCompleted.getOrElse(index) { false }) MintGreen else Color(0xFFF0F0F0)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        if (weekCompleted.getOrElse(index) { false }) {
                                            Text("✓", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(day, fontSize = 10.sp, color = if (weekCompleted.getOrElse(index) { false }) Gray700 else Gray400)
                            }
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("成就收集进度", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gray700)
                        Text("$unlockedCount / $totalCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MintGreen, trackColor = Color(0xFFF0F0F0)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        AchievementTier.values().forEach { tier ->
                            val tierCount = allAchievements.count { it.tier == tier && it.unlocked }
                            val tierTotal = allAchievements.count { it.tier == tier }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$tierCount/$tierTotal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tier.badgeColor)
                                Text(tier.label, fontSize = 9.sp, color = Gray400)
                            }
                        }
                    }
                }
            }

            if (unlockedAchievements.isNotEmpty()) {
                Text("已解锁成就 (${unlockedAchievements.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                unlockedAchievements.forEach { AchievementCard(it) }
            }

            if (lockedAchievements.isNotEmpty()) {
                Text("待解锁成就 (${lockedAchievements.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                lockedAchievements.forEach { AchievementCard(it) }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun buildAchievementList(
    totalRecords: Int,
    consecutiveDays: Int,
    totalScanDays: Int,
    bestDaySugar: Float,
    firstRecordDate: String
): List<AchievementItem> {
    val list = mutableListOf<AchievementItem>()

    fun add(icon: String, title: String, desc: String, unlocked: Boolean, tier: AchievementTier, cat: String, progress: String? = null) {
        list.add(AchievementItem(
            icon = if (unlocked) icon else "🔒",
            title = title, description = desc,
            date = if (unlocked) firstRecordDate.takeLast(5) else "",
            unlocked = unlocked, progress = progress, tier = tier, category = cat
        ))
    }

    add("🌱", "初见甜蜜", "完成首次食物记录，控糖之旅从这里开始", totalRecords >= 1, AchievementTier.BRONZE, "记录",
        if (totalRecords < 1) "$totalRecords/1" else null)
    add("📝", "认真记录", "累计记录10条饮食数据", totalRecords >= 10, AchievementTier.BRONZE, "记录",
        if (totalRecords < 10) "$totalRecords/10" else null)
    add("📊", "数据达人", "累计记录50条饮食数据，养成好习惯", totalRecords >= 50, AchievementTier.SILVER, "记录",
        if (totalRecords in 10..49) "$totalRecords/50" else null)
    add("💯", "百条记录", "累计记录100条，你是最认真的记录者", totalRecords >= 100, AchievementTier.GOLD, "记录",
        if (totalRecords in 50..99) "$totalRecords/100" else null)
    add("📚", "记录狂魔", "累计记录200条，简直停不下来", totalRecords >= 200, AchievementTier.GOLD, "记录",
        if (totalRecords in 100..199) "$totalRecords/200" else null)
    add("🏛️", "数据之王", "累计记录500条，你就是行走的数据库", totalRecords >= 500, AchievementTier.DIAMOND, "记录",
        if (totalRecords in 200..499) "$totalRecords/500" else null)
    add("👑", "传奇记录者", "累计记录1000条，前无古人", totalRecords >= 1000, AchievementTier.LEGENDARY, "记录",
        if (totalRecords in 500..999) "$totalRecords/1000" else null)

    add("🍃", "初试身手", "连续3天糖分不超标", consecutiveDays >= 3, AchievementTier.BRONZE, "连续达标",
        if (consecutiveDays < 3) "$consecutiveDays/3" else null)
    add("⭐", "一周冠军", "连续7天控糖达标，你超棒的", consecutiveDays >= 7, AchievementTier.BRONZE, "连续达标",
        if (consecutiveDays in 3..6) "$consecutiveDays/7" else null)
    add("🔥", "两周挑战", "连续14天达标，意志力超群", consecutiveDays >= 14, AchievementTier.SILVER, "连续达标",
        if (consecutiveDays in 7..13) "$consecutiveDays/14" else null)
    add("💪", "三周勇士", "连续21天达标，习惯已养成", consecutiveDays >= 21, AchievementTier.SILVER, "连续达标",
        if (consecutiveDays in 14..20) "$consecutiveDays/21" else null)
    add("🏅", "月度达人", "连续30天达标，控糖已是生活方式", consecutiveDays >= 30, AchievementTier.GOLD, "连续达标",
        if (consecutiveDays in 21..29) "$consecutiveDays/30" else null)
    add("🌟", "双月之星", "连续60天达标，坚持就是胜利", consecutiveDays >= 60, AchievementTier.GOLD, "连续达标",
        if (consecutiveDays in 30..59) "$consecutiveDays/60" else null)
    add("💎", "百日传奇", "连续100天达标，你是控糖之神", consecutiveDays >= 100, AchievementTier.DIAMOND, "连续达标",
        if (consecutiveDays in 60..99) "$consecutiveDays/100" else null)
    add("🐉", "半年霸主", "连续180天达标，半年如一日", consecutiveDays >= 180, AchievementTier.LEGENDARY, "连续达标",
        if (consecutiveDays in 100..179) "$consecutiveDays/180" else null)

    add("📅", "签到新手", "累计7天有记录数据", totalScanDays >= 7, AchievementTier.BRONZE, "打卡",
        if (totalScanDays < 7) "$totalScanDays/7" else null)
    add("🗓️", "半月达人", "累计14天有记录数据", totalScanDays >= 14, AchievementTier.BRONZE, "打卡",
        if (totalScanDays in 7..13) "$totalScanDays/14" else null)
    add("📆", "坚持一个月", "累计30天有记录，一个月的坚守", totalScanDays >= 30, AchievementTier.SILVER, "打卡",
        if (totalScanDays in 14..29) "$totalScanDays/30" else null)
    add("🎯", "两个月老手", "累计60天有记录", totalScanDays >= 60, AchievementTier.SILVER, "打卡",
        if (totalScanDays in 30..59) "$totalScanDays/60" else null)
    add("🎖️", "季度之星", "累计90天有记录数据", totalScanDays >= 90, AchievementTier.GOLD, "打卡",
        if (totalScanDays in 60..89) "$totalScanDays/90" else null)
    add("🏆", "半年勋章", "累计180天有记录，真正的自律者", totalScanDays >= 180, AchievementTier.DIAMOND, "打卡",
        if (totalScanDays in 90..179) "$totalScanDays/180" else null)
    add("🌈", "年度传说", "累计365天有记录，一整年的守护", totalScanDays >= 365, AchievementTier.LEGENDARY, "打卡",
        if (totalScanDays in 180..364) "$totalScanDays/365" else null)

    add("🧃", "减糖先锋", "单日糖分低于20g", bestDaySugar in 0.01f..20f, AchievementTier.BRONZE, "挑战")
    add("🥛", "低糖达人", "单日糖分低于15g", bestDaySugar in 0.01f..15f, AchievementTier.SILVER, "挑战")
    add("🍵", "极限控糖", "单日糖分低于10g", bestDaySugar in 0.01f..10f, AchievementTier.GOLD, "挑战")
    add("💧", "无糖勇者", "单日糖分低于5g，真正的自律王", bestDaySugar in 0.01f..5f, AchievementTier.DIAMOND, "挑战")

    add("🎓", "健康学霸", "坚持记录+达标，全方面优秀",
        totalScanDays >= 30 && consecutiveDays >= 7, AchievementTier.SILVER, "综合")
    add("🦸", "控糖超人", "50条记录+14天连续达标",
        totalRecords >= 50 && consecutiveDays >= 14, AchievementTier.GOLD, "综合")
    add("🧙", "控糖大师", "100条记录+30天连续达标+60天打卡",
        totalRecords >= 100 && consecutiveDays >= 30 && totalScanDays >= 60, AchievementTier.DIAMOND, "综合")
    add("✨", "糖知之光", "终极成就：200条记录+60天连续达标+90天打卡",
        totalRecords >= 200 && consecutiveDays >= 60 && totalScanDays >= 90, AchievementTier.LEGENDARY, "综合")

    return list
}

@Composable
private fun AchievementCard(achievement: AchievementItem) {
    val tier = achievement.tier
    Surface(
        modifier = Modifier.fillMaxWidth().alpha(if (achievement.unlocked) 1f else 0.55f),
        shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(48.dp)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (achievement.unlocked) tier.bgColors[0] else Gray100
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(achievement.icon, fontSize = 24.sp)
                    }
                }
                if (achievement.unlocked) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(16.dp),
                        shape = CircleShape,
                        color = tier.badgeColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                when (tier) {
                                    AchievementTier.BRONZE -> "I"
                                    AchievementTier.SILVER -> "II"
                                    AchievementTier.GOLD -> "III"
                                    AchievementTier.DIAMOND -> "IV"
                                    AchievementTier.LEGENDARY -> "V"
                                },
                                fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(achievement.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                    if (achievement.unlocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = tier.badgeColor.copy(alpha = 0.15f)) {
                            Text(tier.label, fontSize = 9.sp, color = tier.badgeColor, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(achievement.description, fontSize = 12.sp, color = Color(0xFF999999))
            }
            if (achievement.unlocked) {
                Surface(shape = RoundedCornerShape(8.dp), color = MintBg) {
                    Text("已达成", fontSize = 10.sp, color = MintGreen, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            } else {
                achievement.progress?.let { progress ->
                    val parts = progress.split("/")
                    val current = parts[0].toFloatOrNull() ?: 0f
                    val total = parts[1].toFloatOrNull() ?: 1f
                    val percent = (current / total).coerceIn(0f, 1f)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(progress, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gray400, textAlign = TextAlign.End)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(progress = percent, modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = MintGreen, trackColor = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}
