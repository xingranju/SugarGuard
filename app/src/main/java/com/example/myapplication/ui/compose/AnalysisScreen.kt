package com.example.myapplication.ui.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.ChatRequest
import com.example.myapplication.model.ChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private fun androidx.compose.ui.geometry.Rect.toAndroidRect(): Rect =
    Rect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())

private fun combineBitmapsVertical(parts: List<Bitmap>): Bitmap? {
    if (parts.isEmpty()) return null
    val w = parts.maxOf { it.width }
    val h = parts.sumOf { it.height }
    val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = AndroidCanvas(out)
    var y = 0
    for (p in parts) {
        c.drawBitmap(p, 0f, y.toFloat(), null)
        y += p.height
    }
    return out
}

/**
 * 使用 animateScrollTo 将滚动条对齐到目标偏移，确保帧渲染完成后再返回。
 */
private suspend fun scrollStateSnapTo(state: ScrollState, target: Int) {
    val t = target.coerceIn(0, state.maxValue)
    state.animateScrollTo(t, androidx.compose.animation.core.tween(0))
}

private suspend fun pixelCopyWindowRect(window: Window, rect: Rect): Bitmap? =
    suspendCoroutine { cont ->
        if (rect.width() <= 0 || rect.height() <= 0) {
            cont.resume(null)
            return@suspendCoroutine
        }
        val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
        PixelCopy.request(window, rect, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) cont.resume(bitmap)
            else {
                bitmap.recycle()
                cont.resume(null)
            }
        }, Handler(Looper.getMainLooper()))
    }

/**
 * 分段滚动截取可滚动区域并纵向拼接；可选拼接顶部固定区（标题+周期 Tab）。
 */
private suspend fun buildAnalysisLongScreenshot(
    window: Window,
    scrollState: ScrollState,
    headerRect: Rect?,
    scrollRect: Rect?
): Bitmap? {
    if (scrollRect == null || scrollRect.width() <= 0 || scrollRect.height() <= 0) return null

    val viewportH = scrollRect.height()
    val maxScroll = scrollState.maxValue
    if (maxScroll < 0) return null

    val strips = mutableListOf<Bitmap>()
    val savedScroll = scrollState.value

    scrollStateSnapTo(scrollState, 0)
    delay(200)

    var prevActualScroll = scrollState.value
    val firstStrip = pixelCopyWindowRect(window, scrollRect)
    if (firstStrip != null) strips.add(firstStrip)

    var targetScroll = viewportH
    while (targetScroll <= maxScroll) {
        scrollStateSnapTo(scrollState, targetScroll)
        delay(200)
        val actualScroll = scrollState.value
        val delta = actualScroll - prevActualScroll
        if (delta <= 0) break

        val strip = pixelCopyWindowRect(window, scrollRect) ?: break
        val skipTop = (viewportH - delta).coerceAtLeast(0)
        val takeH = strip.height - skipTop
        if (takeH > 0 && skipTop < strip.height) {
            val cropped = Bitmap.createBitmap(strip, 0, skipTop, strip.width, takeH)
            strip.recycle()
            strips.add(cropped)
        } else {
            strips.add(strip)
        }
        prevActualScroll = actualScroll
        targetScroll += viewportH
    }

    if (prevActualScroll < maxScroll) {
        scrollStateSnapTo(scrollState, maxScroll)
        delay(200)
        val actualScroll = scrollState.value
        val delta = actualScroll - prevActualScroll
        if (delta > 0) {
            val strip = pixelCopyWindowRect(window, scrollRect)
            if (strip != null) {
                val skipTop = (viewportH - delta).coerceAtLeast(0)
                val takeH = strip.height - skipTop
                if (takeH > 0 && skipTop < strip.height) {
                    val cropped = Bitmap.createBitmap(strip, 0, skipTop, strip.width, takeH)
                    strip.recycle()
                    strips.add(cropped)
                } else {
                    strips.add(strip)
                }
            }
        }
    }

    scrollStateSnapTo(scrollState, savedScroll)
    delay(100)

    val scrollCombined = combineBitmapsVertical(strips) ?: return null
    strips.forEach { if (!it.isRecycled) it.recycle() }

    if (headerRect != null && headerRect.width() > 0 && headerRect.height() > 0) {
        val headerBmp = pixelCopyWindowRect(window, headerRect) ?: return scrollCombined
        val merged = combineBitmapsVertical(listOf(headerBmp, scrollCombined))
        headerBmp.recycle()
        scrollCombined.recycle()
        return merged
    }
    return scrollCombined
}

@Composable
fun AnalysisScreen(
    onNavigateToChat: () -> Unit = {},
    onNavigateToHealthProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var selectedPeriod by remember { mutableIntStateOf(1) }
    val periods = listOf("日", "周", "月", "半年")

    var sugarTarget by remember { mutableStateOf(25f) }
    var showSugarDialog by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(25f) }

    if (showSugarDialog) {
        AlertDialog(
            onDismissRequest = { showSugarDialog = false },
            title = { Text("调整每日糖分目标", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("当前目标: ${sugarTarget.toInt()}g/天", fontSize = 14.sp, color = Gray600)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("新目标: ${sliderValue.toInt()}g/天", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 10f..100f,
                        steps = 17,
                        colors = SliderDefaults.colors(thumbColor = MintGreen, activeTrackColor = MintGreen)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("10g", fontSize = 10.sp, color = Gray400)
                        Text("WHO建议: 25g", fontSize = 10.sp, color = MintGreen)
                        Text("100g", fontSize = 10.sp, color = Gray400)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sugarTarget = sliderValue
                        showSugarDialog = false
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    val req = com.example.myapplication.model.HealthProfileRequest(
                                        age = 21, gender = "male", height = 175f, weight = 68f,
                                        sugarLimit = sliderValue, calorieLimit = 2200f, waterGoal = 2000f
                                    )
                                    RetrofitClient.getUserProfileApiService()
                                        .createOrUpdateHealthProfile(req).execute()
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showSugarDialog = false }) { Text("取消", color = Gray500) }
            }
        )
    }
    var todaySugar by remember { mutableStateOf(0.0) }
    var breakfastSugar by remember { mutableStateOf(0.0) }
    var lunchSugar by remember { mutableStateOf(0.0) }
    var dinnerSugar by remember { mutableStateOf(0.0) }
    var snackSugar by remember { mutableStateOf(0.0) }

    var thisWeekData by remember { mutableStateOf(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)) }
    var lastWeekData by remember { mutableStateOf(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)) }

    var monthWeeklyAvgs by remember { mutableStateOf(listOf(0f, 0f, 0f, 0f)) }
    var lastMonthAvg by remember { mutableStateOf(0f) }

    var halfYearMonthlyAvgs by remember { mutableStateOf(listOf(0f, 0f, 0f, 0f, 0f, 0f)) }

    var dayAiAdvice by remember { mutableStateOf("") }
    var weekAiAdvice by remember { mutableStateOf("") }
    var monthAiAdvice by remember { mutableStateOf("") }
    var halfYearAiAdvice by remember { mutableStateOf("") }

    var dayAiConclusion by remember { mutableStateOf("") }
    var dayAiTips by remember { mutableStateOf(listOf<String>()) }
    var weekAiConclusion by remember { mutableStateOf("") }
    var weekAiTips by remember { mutableStateOf(listOf<String>()) }
    var monthAiConclusion by remember { mutableStateOf("") }
    var monthAiTips by remember { mutableStateOf(listOf<String>()) }
    var halfYearAiConclusion by remember { mutableStateOf("") }
    var halfYearAiTips by remember { mutableStateOf(listOf<String>()) }
    var aiLoading by remember { mutableStateOf(false) }

    var weekOverDays by remember { mutableIntStateOf(0) }
    var monthOverDays by remember { mutableIntStateOf(0) }
    var halfYearOverDays by remember { mutableIntStateOf(0) }

    var weekAvgInt by remember { mutableIntStateOf(0) }
    var lastWeekAvgInt by remember { mutableIntStateOf(0) }
    var monthAvgInt by remember { mutableIntStateOf(0) }
    var lastMonthAvgInt by remember { mutableIntStateOf(0) }
    var halfYearAvgInt by remember { mutableIntStateOf(0) }
    var halfYearChangePercent by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()
    var headerWindowRect by remember { mutableStateOf<Rect?>(null) }
    var scrollWindowRect by remember { mutableStateOf<Rect?>(null) }
    var hideFabForShare by remember { mutableStateOf(false) }

    val shareReport: () -> Unit = {
        val act = context as? Activity
        if (act == null) {
            Toast.makeText(context, "截图分享失败: 无法获取界面", Toast.LENGTH_SHORT).show()
        } else {
        scope.launch(Dispatchers.Main) {
            try {
                hideFabForShare = true
                delay(150)
                val window = act.window
                val hdr = headerWindowRect
                val scr = scrollWindowRect
                val bitmap = if (scr != null && scr.height() > 0 && scrollState.maxValue >= 0) {
                    buildAnalysisLongScreenshot(window, scrollState, hdr, scr)
                } else null

                val finalBmp = bitmap ?: run {
                    val rootView = act.window.decorView.rootView
                    val r = Rect(0, 0, rootView.width, rootView.height)
                    pixelCopyWindowRect(window, r)
                }

                hideFabForShare = false
                delay(32)

                if (finalBmp == null) {
                    Toast.makeText(context, "截图失败，请重试", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    try {
                        val file = java.io.File(context.cacheDir, "sugar_guard_report_${System.currentTimeMillis()}.png")
                        java.io.FileOutputStream(file).use { out ->
                            finalBmp.compress(Bitmap.CompressFormat.PNG, 95, out)
                        }
                        finalBmp.recycle()

                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "糖知APP - 控糖分析报告")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "📊 我的糖知控糖分析报告\n\n来「糖知」一起控糖吧！记录每日饮食，轻松管理糖分摄入。"
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        withContext(Dispatchers.Main) {
                            context.startActivity(Intent.createChooser(shareIntent, "分享控糖报告"))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "截图分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                hideFabForShare = false
                Toast.makeText(context, "截图分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        }
    }

    val saveReport: () -> Unit = {
        val act = context as? Activity
        if (act == null) {
            Toast.makeText(context, "保存失败: 无法获取界面", Toast.LENGTH_SHORT).show()
        } else {
        scope.launch(Dispatchers.Main) {
            try {
                hideFabForShare = true
                delay(150)
                val window = act.window
                val hdr = headerWindowRect
                val scr = scrollWindowRect
                val bitmap = if (scr != null && scr.height() > 0 && scrollState.maxValue >= 0) {
                    buildAnalysisLongScreenshot(window, scrollState, hdr, scr)
                } else null
                val finalBmp = bitmap ?: run {
                    val rootView = act.window.decorView.rootView
                    val r = Rect(0, 0, rootView.width, rootView.height)
                    pixelCopyWindowRect(window, r)
                }
                hideFabForShare = false
                delay(32)
                if (finalBmp == null) {
                    Toast.makeText(context, "截图失败，请重试", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    try {
                        val reportsDir = java.io.File(context.filesDir, "saved_reports")
                        if (!reportsDir.exists()) reportsDir.mkdirs()
                        val periodName = listOf("day", "week", "month", "halfyear").getOrElse(selectedPeriod) { "day" }
                        val ts = System.currentTimeMillis()
                        val fileName = "report_${periodName}_${ts}.png"
                        val file = java.io.File(reportsDir, fileName)
                        java.io.FileOutputStream(file).use { out ->
                            finalBmp.compress(Bitmap.CompressFormat.PNG, 95, out)
                        }
                        finalBmp.recycle()
                        val reportPrefs = context.getSharedPreferences("saved_reports", Context.MODE_PRIVATE)
                        val existing = reportPrefs.getString("report_list", "[]") ?: "[]"
                        val periodLabel = listOf("日", "周", "月", "半年").getOrElse(selectedPeriod) { "日" }
                        val entry = """{"path":"${file.absolutePath}","period":"${periodLabel}","date":"${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}","ts":$ts}"""
                        val newList = if (existing == "[]") "[$entry]"
                        else existing.dropLast(1) + ",$entry]"
                        reportPrefs.edit().putString("report_list", newList).apply()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "报告已保存到报告历史", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                hideFabForShare = false
                Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val mealApi = RetrofitClient.getMealApiService()
                    val profileApi = RetrofitClient.getUserProfileApiService()
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    val profileResp = profileApi.getHealthProfile().execute()
                    if (profileResp.isSuccessful && profileResp.body()?.isSuccess == true) {
                        sugarTarget = profileResp.body()?.data?.sugarLimit ?: 25f
                    }

                    fun getDaySugar(dateStr: String): Float {
                        try {
                            val resp = mealApi.getDailyMeals(userId.toInt(), dateStr).execute()
                            if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                val data = resp.body()?.data ?: return 0f
                                @Suppress("UNCHECKED_CAST")
                                val meals = data["meals"] as? List<Map<String, Any?>> ?: return 0f
                                return meals.sumOf {
                                    (it["sugarContent"] as? Number ?: it["sugar_content"] as? Number)?.toDouble() ?: 0.0
                                }.toFloat()
                            }
                        } catch (_: Exception) {}
                        return 0f
                    }

                    fun getDayMealSugarByType(dateStr: String): Map<String, Double> {
                        val result = mutableMapOf("breakfast" to 0.0, "lunch" to 0.0, "dinner" to 0.0, "snack" to 0.0)
                        try {
                            val resp = mealApi.getDailyMeals(userId.toInt(), dateStr).execute()
                            if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                val data = resp.body()?.data ?: return result
                                @Suppress("UNCHECKED_CAST")
                                val meals = data["meals"] as? List<Map<String, Any?>> ?: return result
                                for (m in meals) {
                                    val type = (m["mealType"] as? String ?: m["meal_type"] as? String ?: "snack").lowercase()
                                    val sugar = (m["sugarContent"] as? Number ?: m["sugar_content"] as? Number)?.toDouble() ?: 0.0
                                    result[type] = (result[type] ?: 0.0) + sugar
                                }
                            }
                        } catch (_: Exception) {}
                        return result
                    }

                    val todayStr = sdf.format(Date())
                    val todayBreakdown = getDayMealSugarByType(todayStr)
                    todaySugar = todayBreakdown.values.sum()
                    breakfastSugar = todayBreakdown["breakfast"] ?: 0.0
                    lunchSugar = todayBreakdown["lunch"] ?: 0.0
                    dinnerSugar = todayBreakdown["dinner"] ?: 0.0
                    snackSugar = todayBreakdown["snack"] ?: 0.0

                    val thisWeek = mutableListOf<Float>()
                    val lastWeek = mutableListOf<Float>()
                    for (i in 6 downTo 0) {
                        val c = Calendar.getInstance()
                        c.add(Calendar.DAY_OF_YEAR, -i)
                        thisWeek.add(getDaySugar(sdf.format(c.time)))
                        c.add(Calendar.DAY_OF_YEAR, -7)
                        lastWeek.add(getDaySugar(sdf.format(c.time)))
                    }
                    thisWeekData = thisWeek
                    lastWeekData = lastWeek

                    val weekAvgs = mutableListOf<Float>()
                    for (week in 3 downTo 0) {
                        var weekTotal = 0f; var daysWithData = 0
                        for (day in 6 downTo 0) {
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, -(week * 7 + day))
                            val s = getDaySugar(sdf.format(c.time))
                            weekTotal += s; if (s > 0) daysWithData++
                        }
                        weekAvgs.add(if (daysWithData > 0) weekTotal / daysWithData else 0f)
                    }
                    monthWeeklyAvgs = weekAvgs

                    var lastMonthTotal = 0f; var lastMonthDays = 0
                    for (day in 0 until 30) {
                        val c = Calendar.getInstance()
                        c.add(Calendar.DAY_OF_YEAR, -(30 + day))
                        val s = getDaySugar(sdf.format(c.time))
                        lastMonthTotal += s; if (s > 0) lastMonthDays++
                    }
                    lastMonthAvg = if (lastMonthDays > 0) lastMonthTotal / lastMonthDays else 0f

                    val monthlyAvgs = mutableListOf<Float>()
                    for (month in 5 downTo 0) {
                        var monthTotal = 0f; var daysWithData = 0
                        for (day in 0 until 30) {
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, -(month * 30 + day))
                            val s = getDaySugar(sdf.format(c.time))
                            monthTotal += s; if (s > 0) daysWithData++
                        }
                        monthlyAvgs.add(if (daysWithData > 0) monthTotal / daysWithData else 0f)
                    }
                    halfYearMonthlyAvgs = monthlyAvgs

                    weekOverDays = thisWeek.count { it > sugarTarget }

                    var monthOverCount = 0
                    for (day in 0 until 30) {
                        val c = Calendar.getInstance()
                        c.add(Calendar.DAY_OF_YEAR, -day)
                        if (getDaySugar(sdf.format(c.time)) > sugarTarget) monthOverCount++
                    }
                    monthOverDays = monthOverCount

                    var halfYearOverCount = 0
                    for (day in 0 until 180) {
                        val c = Calendar.getInstance()
                        c.add(Calendar.DAY_OF_YEAR, -day)
                        if (getDaySugar(sdf.format(c.time)) > sugarTarget) halfYearOverCount++
                    }
                    halfYearOverDays = halfYearOverCount

                    val thisWeekAvg = thisWeek.filter { it > 0 }.let { nz ->
                        if (nz.isNotEmpty()) nz.average().toFloat() else 0f
                    }

                    val weekAvg = thisWeekData.filter { it > 0 }.let { if (it.isNotEmpty()) it.average().toInt() else 0 }
                    val monthAvg = monthWeeklyAvgs.filter { it > 0 }.let { if (it.isNotEmpty()) it.average().toInt() else 0 }
                    val halfAvg = halfYearMonthlyAvgs.filter { it > 0 }.let { if (it.isNotEmpty()) it.average().toInt() else 0 }
                    val lastWeekAvgVal = lastWeekData.filter { it > 0 }.let { if (it.isNotEmpty()) it.average().toInt() else 0 }
                    val tgt = sugarTarget.toInt()
                    val ds = todaySugar.toInt()

                    weekAvgInt = weekAvg
                    lastWeekAvgInt = lastWeekAvgVal
                    monthAvgInt = monthAvg
                    lastMonthAvgInt = lastMonthAvg.toInt()
                    halfYearAvgInt = halfAvg
                    val fmHalf = halfYearMonthlyAvgs.firstOrNull { it > 0 } ?: 0f
                    val lmHalf = halfYearMonthlyAvgs.lastOrNull { it > 0 } ?: 0f
                    halfYearChangePercent = if (fmHalf > 0) ((fmHalf - lmHalf) / fmHalf * 100).toInt() else 0

                    dayAiAdvice = "正在获取AI分析..."
                    weekAiAdvice = "正在获取AI分析..."
                    monthAiAdvice = "正在获取AI分析..."
                    halfYearAiAdvice = "正在获取AI分析..."

                    val aiApi = RetrofitClient.getAIApiService()

                    fun callAiForAdvice(prompt: String): String {
                        try {
                            val resp = aiApi.chat(ChatRequest(userId.toInt(), prompt, false)).execute()
                            if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                                return resp.body()?.data?.response ?: ""
                            }
                        } catch (_: Exception) {}
                        return ""
                    }

                    fun parseAiResponse(raw: String): Pair<String, List<String>> {
                        if (raw.isBlank()) return "" to emptyList()
                        val cleaned = raw.replace(Regex("#+\\s*"), "")
                            .replace("**", "").replace("*", "")
                            .trim()
                        val lines = cleaned.lines().map { it.trim() }.filter { it.isNotBlank() }

                        val tipPatterns = listOf("小贴士", "建议", "tips", "试试")
                        val tipStartIdx = lines.indexOfFirst { line ->
                            tipPatterns.any { line.contains(it, ignoreCase = true) }
                        }

                        return if (tipStartIdx > 0) {
                            val mainText = lines.subList(0, tipStartIdx).joinToString("")
                            val tips = lines.subList(tipStartIdx + 1, lines.size)
                                .map { it.removePrefix("-").removePrefix("•").removePrefix("·")
                                    .replace(Regex("^\\d+[.、)）]\\s*"), "").trim() }
                                .filter { it.isNotBlank() }
                                .take(3)
                            mainText to tips
                        } else {
                            val mainText = lines.take((lines.size * 2 / 3).coerceAtLeast(1)).joinToString("")
                            val tips = lines.drop((lines.size * 2 / 3).coerceAtLeast(1))
                                .map { it.removePrefix("-").removePrefix("•").removePrefix("·")
                                    .replace(Regex("^\\d+[.、)）]\\s*"), "").trim() }
                                .filter { it.isNotBlank() }
                                .take(3)
                            mainText to tips
                        }
                    }

                    try {
                        val dayPrompt = "你是控糖健康顾问。用户今日糖分目标${tgt}g，实际摄入${ds}g（早餐${breakfastSugar.toInt()}g，午餐${lunchSugar.toInt()}g，晚餐${dinnerSugar.toInt()}g，加餐${snackSugar.toInt()}g）。请用2-3句话总结今日表现，然后给出3条实用的控糖小贴士。不要使用markdown格式，用纯文本。先写总结，换行后写'小贴士'标题，再逐条列出。"
                        val dayRaw = callAiForAdvice(dayPrompt)
                        val (dayMain, dayTipList) = parseAiResponse(dayRaw)
                        if (dayMain.isNotBlank()) {
                            dayAiAdvice = dayMain.take(50).let { if (it.length < dayMain.length) "$it..." else it }
                            dayAiConclusion = dayMain
                            dayAiTips = dayTipList.ifEmpty { listOf("今天若已喝含糖饮料，晚餐主食可减半拳平衡一下。", "下午加餐改选原味坚果，饱腹感更稳、升糖更慢。", "记录下一餐再决定加餐，避免「不知不觉」超糖。") }
                        } else {
                            dayAiAdvice = if (ds <= tgt) "今日摄入${ds}g，距离目标${tgt}g还有${tgt - ds}g额度。各餐次分布均衡，继续保持。" else "今日摄入${ds}g，超出目标${ds - tgt}g，注意控制。"
                        }
                    } catch (_: Exception) {
                        dayAiAdvice = if (ds <= tgt) "今日摄入${ds}g，距离目标${tgt}g还有${tgt - ds}g额度。" else "今日摄入${ds}g，超出目标${ds - tgt}g。"
                    }

                    try {
                        val weekDiffStr = if (lastWeekAvgVal > 0) "上周日均${lastWeekAvgVal}g" else "无上周数据"
                        val weekPrompt = "你是控糖健康顾问。用户本周日均糖分${weekAvg}g，目标${tgt}g，超标${weekOverDays}天，${weekDiffStr}。请用2-3句话总结本周表现，然后给出3条实用的下周控糖建议。不要使用markdown格式，用纯文本。先写总结，换行后写'小贴士'标题，再逐条列出。"
                        val weekRaw = callAiForAdvice(weekPrompt)
                        val (weekMain, weekTipList) = parseAiResponse(weekRaw)
                        if (weekMain.isNotBlank()) {
                            weekAiAdvice = weekMain.take(50).let { if (it.length < weekMain.length) "$it..." else it }
                            weekAiConclusion = weekMain
                            weekAiTips = weekTipList.ifEmpty { listOf("下次点奶茶试试三分糖？口感差别不大，但每杯少摄入约20g糖", "外卖备注\"少酱\"，酱汁糖分能减35%", "聚餐时带一瓶气泡水，又好看又低糖") }
                        } else {
                            weekAiAdvice = "本周日均${weekAvg}g，${if (weekOverDays > 3) "注意控制超标天数" else "控糖习惯良好"}。"
                        }
                    } catch (_: Exception) {
                        weekAiAdvice = "本周日均${weekAvg}g。"
                    }

                    try {
                        val monthDiffStr = if (lastMonthAvg > 0) "上月日均${lastMonthAvg.toInt()}g" else "无上月数据"
                        val monthPrompt = "你是控糖健康顾问。用户本月日均糖分${monthAvg}g，目标${tgt}g，超标${monthOverDays}天，${monthDiffStr}。请用2-3句话总结本月表现，然后给出3条下月控糖建议。不要使用markdown格式，用纯文本。先写总结，换行后写'小贴士'标题，再逐条列出。"
                        val monthRaw = callAiForAdvice(monthPrompt)
                        val (monthMain, monthTipList) = parseAiResponse(monthRaw)
                        if (monthMain.isNotBlank()) {
                            monthAiAdvice = monthMain.take(50).let { if (it.length < monthMain.length) "$it..." else it }
                            monthAiConclusion = monthMain
                            monthAiTips = monthTipList.ifEmpty { listOf("设定本月「少糖日」每周两天，慢慢变成习惯。", "月底翻一眼趋势图，比只看数字更有成就感。", "囤积几样低糖零食，替代办公室糖果罐。") }
                        } else {
                            monthAiAdvice = "本月日均${monthAvg}g。"
                        }
                    } catch (_: Exception) {
                        monthAiAdvice = "本月日均${monthAvg}g。"
                    }

                    try {
                        val hfFirst = halfYearMonthlyAvgs.firstOrNull { it > 0 }?.toInt() ?: 0
                        val hfLast = halfYearMonthlyAvgs.lastOrNull { it > 0 }?.toInt() ?: 0
                        val halfPrompt = "你是控糖健康顾问。用户近半年日均糖分${halfAvg}g，目标${tgt}g，半年超标${halfYearOverDays}天。首月日均${hfFirst}g，最近月日均${hfLast}g，整体变化${halfYearChangePercent}%。请用2-3句话总结半年趋势，然后给出3条长期健康建议。不要使用markdown格式，用纯文本。先写总结，换行后写'小贴士'标题，再逐条列出。"
                        val halfRaw = callAiForAdvice(halfPrompt)
                        val (halfMain, halfTipList) = parseAiResponse(halfRaw)
                        if (halfMain.isNotBlank()) {
                            halfYearAiAdvice = halfMain.take(50).let { if (it.length < halfMain.length) "$it..." else it }
                            halfYearAiConclusion = halfMain
                            halfYearAiTips = halfTipList.ifEmpty { listOf("每季度设一个小目标，比盯着日曲线更轻松。", "体检糖化血红蛋白时，带上记录截图给医生参考。", "季节换菜谱时，顺便刷新常用外卖与零食清单。") }
                        } else {
                            halfYearAiAdvice = "近半年日均${halfAvg}g。"
                        }
                    } catch (_: Exception) {
                        halfYearAiAdvice = "近半年日均${halfAvg}g。"
                    }

                } catch (_: Exception) {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
        ) {
            Column(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        headerWindowRect = coords.boundsInWindow().toAndroidRect()
                    }
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "健康分析", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray800,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp), color = Gray100
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        periods.forEachIndexed { index, label ->
                            Surface(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPeriod = index },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedPeriod == index) Color.White else Color.Transparent,
                                shadowElevation = if (selectedPeriod == index) 2.dp else 0.dp
                            ) {
                                Text(
                                    label, fontSize = 12.sp,
                                    fontWeight = if (selectedPeriod == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedPeriod == index) MintGreen else Gray400,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { coords ->
                        scrollWindowRect = coords.boundsInWindow().toAndroidRect()
                    }
                    .padding(horizontal = 24.dp)
            ) {
                val openAdjust = { sliderValue = sugarTarget; showSugarDialog = true }
                when (selectedPeriod) {
                    0 -> DayView(
                        todaySugar, sugarTarget, breakfastSugar, lunchSugar, dinnerSugar, snackSugar,
                        dayAiAdvice, dayAiConclusion, dayAiTips, openAdjust, shareReport, saveReport
                    )
                    1 -> WeekView(
                        thisWeekData, sugarTarget, weekOverDays, weekAiAdvice,
                        weekAvgInt, lastWeekAvgInt,
                        weekAiConclusion, weekAiTips,
                        openAdjust, shareReport, saveReport
                    )
                    2 -> MonthView(
                        monthWeeklyAvgs, sugarTarget, monthOverDays, monthAiAdvice,
                        monthAvgInt, lastMonthAvgInt,
                        monthAiConclusion, monthAiTips,
                        openAdjust, shareReport, saveReport
                    )
                    3 -> HalfYearView(
                        halfYearMonthlyAvgs, halfYearOverDays, halfYearAiAdvice,
                        halfYearAvgInt, halfYearChangePercent,
                        halfYearAiConclusion, halfYearAiTips,
                        openAdjust, shareReport, saveReport
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (!hideFabForShare) {
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp)
                    .size(56.dp).shadow(8.dp, CircleShape).clip(CircleShape)
                    .clickable { onNavigateToChat() },
                shape = CircleShape, color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        brush = Brush.linearGradient(listOf(Color(0xFF26A69A), Color(0xFF00897B)))
                    ), contentAlignment = Alignment.Center
                ) { Text("🤖", fontSize = 22.sp) }
            }
        }
    }
}

@Composable
private fun EncouragementBanner(icon: String, title: String, subtitle: String) {
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.getUserInfoApiService().getUserInfo().execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    avatarUrl = resp.body()?.data?.avatarUrl
                }
            } catch (_: Exception) {}
        }
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MintBg) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "头像",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MintGreen) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(icon, fontSize = 18.sp) }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                Text(subtitle, fontSize = 12.sp, color = Gray600, maxLines = 2)
            }
        }
    }
}

@Composable
private fun ActionButtons(onAdjust: () -> Unit, onShareReport: () -> Unit, onSaveReport: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAdjust, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Gray600),
                elevation = ButtonDefaults.buttonElevation(0.dp)) { Text("调整目标", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
            Button(
                onClick = onSaveReport,
                modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) { Text("保存报告", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
            Button(
                onClick = onShareReport,
                modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) { Text("分享报告", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
private fun AiAnalysisConclusionCard(aiAdvice: String, tips: List<String>) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🤖", fontSize = 18.sp)
                Text("AI 分析结论", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(aiAdvice, fontSize = 12.sp, color = Gray600, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MintBg) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("小贴士（试试看？）", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MintGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    tips.forEach { tip -> AiTip(tip) }
                }
            }
        }
    }
}

// ===== Day View =====
@Composable
private fun DayView(
    todaySugar: Double, sugarTarget: Float,
    bf: Double, lc: Double, dn: Double, sn: Double,
    aiAdvice: String,
    aiConclusion: String, aiTips: List<String>,
    onAdjust: () -> Unit,
    onShareReport: () -> Unit,
    onSaveReport: () -> Unit = {}
) {
    val remaining = (sugarTarget - todaySugar.toFloat()).coerceAtLeast(0f)
    val progress = (todaySugar / sugarTarget).toFloat().coerceIn(0f, 1f)
    val isOnTrack = todaySugar <= sugarTarget
    val overDays = if (todaySugar > sugarTarget) 1 else 0
    val dayTips = aiTips.ifEmpty { listOf(
        "今天若已喝含糖饮料，晚餐主食可减半拳平衡一下。",
        "下午加餐改选原味坚果，饱腹感更稳、升糖更慢。",
        "记录下一餐再决定加餐，避免「不知不觉」超糖。"
    ) }

    EncouragementBanner(
        icon = if (isOnTrack) "☀️" else "💪",
        title = "今日: ${todaySugar.toInt()}g / 目标${sugarTarget.toInt()}g",
        subtitle = aiAdvice.take(50).let { if (it.length < aiAdvice.length) "$it..." else it }
    )
    Spacer(modifier = Modifier.height(16.dp))

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("今日累计", fontSize = 12.sp, color = Gray400)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${todaySugar.toInt()}", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Gray800)
                    Text("g", fontSize = 14.sp, color = Gray400, modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("目标 ${sugarTarget.toInt()}g", fontSize = 10.sp, color = Gray400)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(progress = progress, modifier = Modifier.width(128.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (isOnTrack) MintGreen else RedHigh, trackColor = Color(0xFFF5F5F5))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(if (isOnTrack) "剩余额度约 ${remaining.toInt()}g，晚餐建议清淡。" else "已超出目标 ${(todaySugar - sugarTarget).toInt()}g，注意控制。",
                fontSize = 12.sp, color = if (isOnTrack) MintGreen else RedHigh)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("各餐次分布（今日）", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "超标 ${overDays} 天", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (overDays > 0) RedHigh else MintGreen,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End
            )
            BarChartGeneric(listOf(bf.toFloat(), lc.toFloat(), dn.toFloat(), sn.toFloat()), listOf("早", "午", "晚", "加餐"), sugarTarget / 4,
                modifier = Modifier.fillMaxWidth().height(112.dp))
            Spacer(modifier = Modifier.height(8.dp))
            val maxMealName = when (maxOf(bf, lc, dn, sn)) { bf -> "早餐"; lc -> "午餐"; dn -> "晚餐"; else -> "加餐" }
            if (maxOf(bf, lc, dn, sn) > 0) Text("${maxMealName}占比较高。", fontSize = 10.sp, color = Gray400)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    val dayAiText = when {
        aiConclusion.isNotBlank() -> aiConclusion
        aiAdvice.isNotBlank() && aiAdvice != "正在获取AI分析..." -> aiAdvice
        todaySugar == 0.0 -> "今天还没有记录哦，拍照或搜索来记录第一餐吧！"
        isOnTrack -> "今天整体控糖情况不错～继续保持这个节奏，你已经在变得更健康了！"
        else -> "今天摄入稍微超标了，但别担心，一天的波动不影响整体趋势。明天注意一下就好～"
    }
    AiAnalysisConclusionCard(dayAiText, dayTips)
    Spacer(modifier = Modifier.height(16.dp))
    ActionButtons(onAdjust, onShareReport, onSaveReport)
}

// ===== Unified Stats Card =====
@Composable
private fun PeriodStatsCard(periodLabel: String, avgValue: Int, unit: String, lastPeriodAvg: Int, lastPeriodLabel: String) {
    val diff = avgValue - lastPeriodAvg
    val improved = diff <= 0
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(periodLabel, fontSize = 12.sp, color = Gray400)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$avgValue", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Gray800)
                    Text(unit, fontSize = 14.sp, color = Gray400, modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
                }
                if (lastPeriodAvg > 0) {
                    Surface(shape = RoundedCornerShape(20.dp), color = if (improved) MintBg else Color(0xFFFFF3E0)) {
                        Text(
                            "${if (improved) "↓" else "↑"} ${kotlin.math.abs(diff)}g",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (improved) MintGreen else RedHigh,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ===== Unified Chart Card =====
@Composable
private fun PeriodChartCard(
    title: String,
    data: List<Float>,
    labels: List<String>,
    target: Float,
    overDays: Int,
    footerText: String
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                Text(
                    "超标 $overDays 天", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = if (overDays > 0) RedHigh else MintGreen
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LineChartWithLabels(data, labels, target, modifier = Modifier.fillMaxWidth().height(160.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(modifier = Modifier.size(8.dp, 3.dp).clip(RoundedCornerShape(1.dp)), color = MintGreen) {}
                    Text("糖分趋势", fontSize = 10.sp, color = Gray400)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(modifier = Modifier.size(8.dp, 3.dp).clip(RoundedCornerShape(1.dp)), color = Color(0xFFEF9A9A)) {}
                    Text("目标线 (${target.toInt()}g)", fontSize = 10.sp, color = Gray400)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(footerText, fontSize = 10.sp, color = Gray400)
        }
    }
}

// ===== Week View =====
@Composable
private fun WeekView(
    thisWeek: List<Float>, target: Float,
    weekOverDays: Int,
    aiAdvice: String,
    weekAvgInt: Int,
    lastWeekAvgInt: Int,
    aiConclusion: String, aiTips: List<String>,
    onAdjust: () -> Unit,
    onShareReport: () -> Unit,
    onSaveReport: () -> Unit = {}
) {
    val weekDiff = weekAvgInt - lastWeekAvgInt
    val improved = weekDiff <= 0
    val dayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "今天")
    val weekTips = aiTips.ifEmpty { listOf(
        "下次点奶茶试试三分糖？口感差别不大，但每杯少摄入约20g糖",
        "外卖备注\"少酱\"，酱汁糖分能减35%",
        "聚餐时带一瓶气泡水，又好看又低糖"
    ) }

    EncouragementBanner(
        icon = if (improved) "⭐" else "💪",
        title = "本周日均: ${weekAvgInt}g${if (lastWeekAvgInt > 0) "（${if (improved) "↓" else "↑"}${kotlin.math.abs(weekDiff)}g）" else ""}",
        subtitle = aiAdvice.take(50).let { if (it.length < aiAdvice.length) "$it..." else it }
    )
    Spacer(modifier = Modifier.height(16.dp))

    PeriodStatsCard("本周 · 周均糖分", weekAvgInt, "g/日", lastWeekAvgInt, "上周")
    Spacer(modifier = Modifier.height(16.dp))

    PeriodChartCard(
        title = "七日趋势",
        data = thisWeek,
        labels = dayLabels,
        target = target,
        overDays = weekOverDays,
        footerText = if (thisWeek.size >= 2 && thisWeek.last() < thisWeek.first()) "近期控糖效果逐步改善。" else "保持记录习惯，趋势会越来越清晰。"
    )
    Spacer(modifier = Modifier.height(16.dp))

    val weekAiText = when {
        aiConclusion.isNotBlank() -> aiConclusion
        aiAdvice.isNotBlank() && aiAdvice != "正在获取AI分析..." -> aiAdvice
        weekAvgInt > 0 && lastWeekAvgInt > 0 && weekDiff < 0 ->
            "你这周做得不错哦！日均糖分比上周下降了${-weekDiff}g，说明你的控糖意识在变强，继续保持这个势头～"
        weekAvgInt == 0 ->
            "本周暂时没有足够的记录数据，坚持每天记录可以帮助AI给出更准确的分析哦～"
        else ->
            "这周日均糖分略有上升，可能是社交聚餐比较多。不用太紧张，下周注意控制饮料摄入就好～"
    }
    AiAnalysisConclusionCard(weekAiText, weekTips)
    Spacer(modifier = Modifier.height(16.dp))
    ActionButtons(onAdjust, onShareReport, onSaveReport)
}

// ===== Month View =====
@Composable
private fun MonthView(
    weekAvgs: List<Float>, target: Float,
    monthOverDays: Int,
    aiAdvice: String,
    monthAvgInt: Int,
    lastMonthAvgInt: Int,
    aiConclusion: String, aiTips: List<String>,
    onAdjust: () -> Unit,
    onShareReport: () -> Unit,
    onSaveReport: () -> Unit = {}
) {
    val monthDiff = monthAvgInt - lastMonthAvgInt
    val improved = monthDiff <= 0
    val weekLabels = listOf("第1周", "第2周", "第3周", "第4周")
    val now = LocalDate.now()
    val monthStr = now.format(DateTimeFormatter.ofPattern("yyyy年M月"))
    val monthTips = aiTips.ifEmpty { listOf(
        "设定本月「少糖日」每周两天，慢慢变成习惯。",
        "月底翻一眼趋势图，比只看数字更有成就感。",
        "囤积几样低糖零食，替代办公室糖果罐。"
    ) }

    EncouragementBanner(
        icon = if (improved) "🏆" else "💪",
        title = "本月日均: ${monthAvgInt}g${if (lastMonthAvgInt > 0) "（${if (improved) "↓" else "↑"}${kotlin.math.abs(monthDiff)}g）" else ""}",
        subtitle = aiAdvice.take(50).let { if (it.length < aiAdvice.length) "$it..." else it }
    )
    Spacer(modifier = Modifier.height(16.dp))

    PeriodStatsCard("$monthStr · 月均糖分", monthAvgInt, "g/日", lastMonthAvgInt, "上月")
    Spacer(modifier = Modifier.height(16.dp))

    PeriodChartCard(
        title = "四周对比",
        data = weekAvgs,
        labels = weekLabels,
        target = target,
        overDays = monthOverDays,
        footerText = if (weekAvgs.size >= 2 && weekAvgs.last() < weekAvgs.first()) "近期控糖效果逐步改善。" else "保持记录习惯，趋势会越来越清晰。"
    )
    Spacer(modifier = Modifier.height(16.dp))

    val monthAiText = when {
        aiConclusion.isNotBlank() -> aiConclusion
        aiAdvice.isNotBlank() && aiAdvice != "正在获取AI分析..." -> aiAdvice
        monthAvgInt == 0 -> "本月数据还不够，坚持记录就能看到完整的月度分析哦～"
        improved -> "这个月你做得真的很棒！总量一直在下降，下个月试试挑战周均 ≤${(monthAvgInt - 2).coerceAtLeast(15)}g？我觉得你完全可以～"
        else -> "这个月稍有波动，但这很正常。保持记录习惯，趋势会越来越好的～"
    }
    AiAnalysisConclusionCard(monthAiText, monthTips)
    Spacer(modifier = Modifier.height(16.dp))
    ActionButtons(onAdjust, onShareReport, onSaveReport)
}

// ===== Half Year View =====
@Composable
private fun HalfYearView(
    monthlyAvgs: List<Float>,
    halfYearOverDays: Int,
    aiAdvice: String,
    halfYearAvgInt: Int,
    halfYearChangePercent: Int,
    aiConclusion: String, aiTips: List<String>,
    onAdjust: () -> Unit,
    onShareReport: () -> Unit,
    onSaveReport: () -> Unit = {}
) {
    val now = LocalDate.now()
    val monthLabels = (5 downTo 0).map { now.minusMonths(it.toLong()).format(DateTimeFormatter.ofPattern("M月")) }
    val halfYearTips = aiTips.ifEmpty { listOf(
        "每季度设一个小目标，比盯着日曲线更轻松。",
        "体检糖化血红蛋白时，带上记录截图给医生参考。",
        "季节换菜谱时，顺便刷新常用外卖与零食清单。"
    ) }

    EncouragementBanner(
        icon = if (halfYearChangePercent >= 0) "🚀" else "💪",
        title = "半年日均: ${halfYearAvgInt}g${if (halfYearChangePercent != 0) "（${if (halfYearChangePercent > 0) "↓" else "↑"}${kotlin.math.abs(halfYearChangePercent)}%）" else ""}",
        subtitle = aiAdvice.take(50).let { if (it.length < aiAdvice.length) "$it..." else it }
    )
    Spacer(modifier = Modifier.height(16.dp))

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("近 6 个月 · 月均糖分", fontSize = 12.sp, color = Gray400)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$halfYearAvgInt", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Gray800)
                    Text("g/日", fontSize = 14.sp, color = Gray400, modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
                }
                if (halfYearChangePercent != 0) {
                    Surface(shape = RoundedCornerShape(20.dp), color = if (halfYearChangePercent > 0) MintBg else Color(0xFFFFF3E0)) {
                        Text(
                            "整体 ${if (halfYearChangePercent > 0) "↓" else "↑"} ${kotlin.math.abs(halfYearChangePercent)}%",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (halfYearChangePercent > 0) MintGreen else RedHigh,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    PeriodChartCard(
        title = "半年趋势（按月）",
        data = monthlyAvgs,
        labels = monthLabels,
        target = 25f,
        overDays = halfYearOverDays,
        footerText = "数据来自您每日的记录汇总。"
    )
    Spacer(modifier = Modifier.height(16.dp))

    val halfAiText = when {
        aiConclusion.isNotBlank() -> aiConclusion
        aiAdvice.isNotBlank() && aiAdvice != "正在获取AI分析..." -> aiAdvice
        halfYearAvgInt == 0 -> "坚持记录每日饮食，半年后你会看到令人惊喜的变化趋势～"
        halfYearChangePercent > 10 -> "从半年的角度来看，你的控糖之路稳步前进中。建议下次体检时带上你的控糖报告，让医生也看看你的进步～"
        else -> "保持目前的记录习惯，稳步调整饮食结构。长期坚持比短期突击更有效，你已经在正确的路上了～"
    }
    AiAnalysisConclusionCard(halfAiText, halfYearTips)
    Spacer(modifier = Modifier.height(16.dp))
    ActionButtons(onAdjust, onShareReport, onSaveReport)
}

// ===== Chart Components =====
@Composable
private fun LineChartWithLabels(data: List<Float>, labels: List<String>, target: Float, modifier: Modifier = Modifier) {
    val maxVal = (data.maxOrNull() ?: 50f).coerceAtLeast(target * 1.3f)
    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val chartPaddingBottom = 24.dp.toPx()
            val chartHeight = size.height - chartPaddingBottom
            val pointSpacing = size.width / (data.size - 1).coerceAtLeast(1)

            val points = data.mapIndexed { index, value ->
                val x = index * pointSpacing
                val y = chartHeight - (if (maxVal > 0) (value / maxVal) * chartHeight * 0.9f else 0f)
                Offset(x, y)
            }

            val targetY = chartHeight - (target / maxVal) * chartHeight * 0.9f
            drawLine(
                Color(0xFFEF9A9A),
                Offset(0f, targetY), Offset(size.width, targetY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )

            if (points.size >= 2) {
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, chartHeight)
                    lineTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val cpX = (points[i - 1].x + points[i].x) / 2
                        cubicTo(cpX, points[i - 1].y, cpX, points[i].y, points[i].x, points[i].y)
                    }
                    lineTo(points.last().x, chartHeight)
                    close()
                }
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(MintGreen.copy(alpha = 0.25f), MintGreen.copy(alpha = 0.02f)),
                        startY = 0f, endY = chartHeight
                    )
                )

                val linePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val cpX = (points[i - 1].x + points[i].x) / 2
                        cubicTo(cpX, points[i - 1].y, cpX, points[i].y, points[i].x, points[i].y)
                    }
                }
                drawPath(linePath, color = MintGreen, style = Stroke(width = 2.5.dp.toPx()))
            }

            points.forEachIndexed { index, point ->
                val value = data[index]
                if (value > 0) {
                    drawCircle(Color.White, radius = 5.dp.toPx(), center = point)
                    drawCircle(
                        color = if (value > target) RedHigh else MintGreen,
                        radius = 3.5.dp.toPx(), center = point
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { label ->
                Text(label, fontSize = 9.sp, color = Gray400, textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BarChartGeneric(data: List<Float>, labels: List<String>, target: Float, modifier: Modifier = Modifier) {
    val maxVal = (data.maxOrNull() ?: 50f).coerceAtLeast(target * 1.2f)
    Canvas(modifier = modifier) {
        val barWidth = size.width / (data.size * 2f)
        val spacing = barWidth
        data.forEachIndexed { index, value ->
            val barHeight = if (maxVal > 0) (value / maxVal) * size.height * 0.85f else 0f
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight
            drawRoundRect(
                color = if (value > target) RedHigh else MintGreen.copy(alpha = if (value > 0) 0.6f else 0.15f),
                topLeft = Offset(x, y), size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
        val targetY = size.height - (target / maxVal) * size.height * 0.85f
        drawLine(Color(0xFFEF9A9A), Offset(0f, targetY), Offset(size.width, targetY),
            strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
    }
}

@Composable
private fun AiTip(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("✨", fontSize = 12.sp); Text(text, fontSize = 12.sp, color = Gray600, lineHeight = 18.sp)
    }
}

