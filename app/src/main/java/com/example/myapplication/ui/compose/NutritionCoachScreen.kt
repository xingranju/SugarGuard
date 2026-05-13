package com.example.myapplication.ui.compose

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.HealthReportDto
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.util.ReportPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CoachGreen = Color(0xFF26A69A)
private val CoachDark = Color(0xFF00796B)
private val CardBg = Color(0xFFFFFFFF)
private val PageBg = Color(0xFFF5F7FA)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B7280)
private val TextMuted = Color(0xFF9CA3AF)
private val DividerColor = Color(0xFFF0F0F0)
private val ScoreExcellent = Color(0xFF059669)
private val ScoreGood = Color(0xFF26A69A)
private val ScoreWarning = Color(0xFFD97706)
private val ScoreDanger = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionCoachScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var selectedPeriod by remember { mutableStateOf("weekly") }
    var weeklyReport by remember { mutableStateOf<HealthReportDto?>(null) }
    var monthlyReport by remember { mutableStateOf<HealthReportDto?>(null) }
    var loading by remember { mutableStateOf(false) }

    val currentReport = if (selectedPeriod == "weekly") weeklyReport else monthlyReport

    fun loadCachedReport(period: String): HealthReportDto? {
        val rPrefs = context.getSharedPreferences("nutrition_coach", Context.MODE_PRIVATE)
        val json = rPrefs.getString("report_${period}_$userId", null) ?: return null
        return try {
            com.google.gson.Gson().fromJson(json, HealthReportDto::class.java)
        } catch (_: Exception) { null }
    }

    fun cacheReport(period: String, report: HealthReportDto) {
        val rPrefs = context.getSharedPreferences("nutrition_coach", Context.MODE_PRIVATE)
        rPrefs.edit().putString("report_${period}_$userId",
            com.google.gson.Gson().toJson(report)).apply()
    }

    fun generateReport(period: String) {
        if (userId <= 0L) {
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }
        loading = true
        scope.launch {
            try {
                val report = withContext(Dispatchers.IO) {
                    val resp = RetrofitClient.getReportApiService()
                        .generateAiReport(userId, period).execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                        resp.body()?.data
                    } else null
                }
                if (report != null) {
                    cacheReport(period, report)
                    if (period == "weekly") weeklyReport = report else monthlyReport = report
                    Toast.makeText(context, "报告生成成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "报告生成失败，请稍后重试", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "网络错误: ${e.message?.take(40)}", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        weeklyReport = loadCachedReport("weekly")
        monthlyReport = loadCachedReport("monthly")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(CoachGreen, CoachDark)
                    )
                )
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "AI 营养教练",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "智能周报 · 月报生成器",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PeriodTab(
                        label = "本周报告",
                        selected = selectedPeriod == "weekly",
                        onClick = { selectedPeriod = "weekly" },
                        modifier = Modifier.weight(1f)
                    )
                    PeriodTab(
                        label = "本月报告",
                        selected = selectedPeriod == "monthly",
                        onClick = { selectedPeriod = "monthly" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (loading) {
            LoadingView()
        } else if (currentReport != null) {
            ReportContent(
                report = currentReport,
                periodLabel = if (selectedPeriod == "weekly") "周" else "月",
                onRefresh = { generateReport(selectedPeriod) },
                onShare = { shareReportText(context, currentReport, selectedPeriod) },
                onExportPdf = {
                    val pLabel = if (selectedPeriod == "weekly") "周" else "月"
                    val pdfData = ReportPdfGenerator.ReportData(
                        title = "糖知 AI 营养教练 · 本${pLabel}报告",
                        periodLabel = pLabel,
                        dateRange = "${currentReport.startDate ?: ""} ~ ${currentReport.endDate ?: ""}",
                        score = currentReport.score ?: 0,
                        avgSugar = currentReport.avgSugar ?: 0f,
                        avgCalories = currentReport.avgCalories ?: 0f,
                        sugarLimit = currentReport.sugarLimit ?: 25f,
                        overDays = currentReport.overDays ?: 0,
                        totalDays = currentReport.totalDays ?: 0,
                        recordDays = currentReport.recordDays ?: 0,
                        aiReport = currentReport.aiReport
                    )
                    ReportPdfGenerator.generateAndShare(context, pdfData)
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            EmptyView(
                periodLabel = if (selectedPeriod == "weekly") "周" else "月",
                onGenerate = { generateReport(selectedPeriod) }
            )
        }
    }
}

@Composable
private fun PeriodTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.12f),
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) CoachGreen else Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun LoadingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = CoachGreen,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "AI 教练正在分析数据...",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary.copy(alpha = alpha)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "通常需要 15-30 秒",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ReportContent(
    report: HealthReportDto,
    periodLabel: String,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Spacer(modifier = Modifier.height(16.dp))

        ScoreSection(report, periodLabel)

        Spacer(modifier = Modifier.height(16.dp))

        StatsSection(report)

        if (!report.aiReport.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            AiReportSection(report.aiReport)
        }

        Spacer(modifier = Modifier.height(20.dp))

        ActionButtons(onRefresh, onShare, onExportPdf)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ScoreSection(report: HealthReportDto, periodLabel: String) {
    val score = report.score ?: 0
    val scoreColor = when {
        score >= 90 -> ScoreExcellent
        score >= 70 -> ScoreGood
        score >= 50 -> ScoreWarning
        else -> ScoreDanger
    }
    val scoreLabel = when {
        score >= 90 -> "优秀"
        score >= 70 -> "良好"
        score >= 50 -> "一般"
        else -> "需改善"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "本${periodLabel}控糖评分",
                fontSize = 13.sp,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                val animatedProgress = remember { Animatable(0f) }
                LaunchedEffect(score) {
                    animatedProgress.animateTo(
                        targetValue = score / 100f,
                        animationSpec = tween(1200, easing = EaseOutCubic)
                    )
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    drawArc(
                        color = Color(0xFFF3F4F6),
                        startAngle = -225f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = -225f,
                        sweepAngle = 270f * animatedProgress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$score",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        scoreLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "${report.startDate ?: ""} ~ ${report.endDate ?: ""}",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun StatsSection(report: HealthReportDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "日均糖分",
                value = "%.1fg".format(report.avgSugar ?: 0f),
                detail = "目标 %.0fg".format(report.sugarLimit ?: 25f),
                accent = if ((report.avgSugar ?: 0f) <= (report.sugarLimit ?: 25f))
                    CoachGreen else ScoreWarning,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "日均热量",
                value = "%.0fkcal".format(report.avgCalories ?: 0f),
                detail = "${report.recordDays ?: 0}天数据",
                accent = CoachGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "超标天数",
                value = "${report.overDays ?: 0}天",
                detail = "共${report.totalDays ?: 0}天",
                accent = if ((report.overDays ?: 0) == 0) ScoreExcellent else ScoreWarning,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "记录天数",
                value = "${report.recordDays ?: 0}天",
                detail = "共${report.totalDays ?: 0}天",
                accent = CoachGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(detail, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun AiReportSection(aiReport: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoachGreen.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CoachGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "AI 营养教练分析",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "基于您的饮食数据智能生成",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = DividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            FormattedReport(aiReport)
        }
    }
}

@Composable
private fun FormattedReport(text: String) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                continue
            }
            when {
                trimmed.startsWith("【") && trimmed.endsWith("】") -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        trimmed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoachGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                trimmed.matches(Regex("^[一二三四五六七八九十]+[、.．].*")) -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        trimmed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                trimmed.matches(Regex("^\\d+[.、．)].*")) -> {
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        val num = trimmed.takeWhile { it.isDigit() }
                        Surface(
                            shape = CircleShape,
                            color = CoachGreen,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    num,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        val content = trimmed.dropWhile { it.isDigit() }
                            .trimStart('.', '、', '．', ')', ' ')
                        Text(
                            content,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("· ") -> {
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(CoachGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            trimmed.drop(2),
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    Text(
                        trimmed,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(onRefresh: () -> Unit, onShare: () -> Unit, onExportPdf: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoachGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("重新生成", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onShare,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoachGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("分享文本", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        Button(
            onClick = onExportPdf,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("导出 PDF 报告", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun EmptyView(periodLabel: String, onGenerate: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = CoachGreen.copy(alpha = 0.08f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = CoachGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "暂无本${periodLabel}报告",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "AI 营养教练将分析您的饮食数据\n生成专属的控糖健康报告",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGenerate,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoachGreen),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "生成本${periodLabel}报告",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun shareReportText(context: Context, report: HealthReportDto, period: String) {
    val periodLabel = if (period == "weekly") "周" else "月"
    val text = buildString {
        appendLine("糖知 AI 营养教练 · 本${periodLabel}报告")
        appendLine("━━━━━━━━━━━━━━━━")
        appendLine("${report.startDate} ~ ${report.endDate}")
        appendLine("控糖评分: ${report.score}/100")
        appendLine("日均糖分: ${"%.1f".format(report.avgSugar)}g (目标${"%.0f".format(report.sugarLimit)}g)")
        appendLine("日均热量: ${"%.0f".format(report.avgCalories)}kcal")
        appendLine("超标天数: ${report.overDays}/${report.totalDays}天")
        appendLine()
        if (!report.aiReport.isNullOrBlank()) {
            appendLine(report.aiReport)
        }
        appendLine()
        appendLine("—— 来自「糖知」AI 营养教练")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享 AI 营养报告"))
}
