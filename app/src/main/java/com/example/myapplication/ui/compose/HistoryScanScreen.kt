package com.example.myapplication.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.api.RecentMealDto
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val API_ORIGIN = "http://10.0.2.2:8080"

private data class ScanRecord(
    val mealId: Int?,
    val mealDate: LocalDate,
    val icon: String,
    val name: String,
    val sugar: String,
    val calories: String,
    val time: String,
    /** HH:mm:ss，用于同日排序 */
    val timeSortKey: String,
    val imageUrl: String?,
    val aiAdvice: String? = null
)

private data class DayGroup(
    val sortKey: LocalDate,
    val label: String,
    val records: List<ScanRecord>
)

private enum class ScanFilterMode { ALL, WITH_IMAGE, TEXT_ONLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScanScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var dayGroups by remember { mutableStateOf<List<DayGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var daysWindow by remember { mutableIntStateOf(30) }
    var filterMode by remember { mutableStateOf(ScanFilterMode.ALL) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1).toInt()

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val dateDisplayFormatter = DateTimeFormatter.ofPattern("M月d日")

    fun RecentMealDto.toScanRecord(date: LocalDate): ScanRecord {
        val sugarVal = sugarContent ?: 0.0
        val calVal = calories ?: 0.0
        val path = imagePath?.trim().orEmpty()
        val resolved = resolveImageUrl(path)
        val rawTime = mealTime ?: "00:00:00"
        val advice = parseAiAdvice(notes)
        return ScanRecord(
            mealId = mealId,
            mealDate = date,
            icon = getMealTypeEmoji(mealType ?: "snack"),
            name = foodName.orEmpty(),
            sugar = "${sugarVal.toInt()}g",
            calories = "${calVal.toInt()} kcal",
            time = rawTime.take(5),
            timeSortKey = rawTime,
            imageUrl = resolved,
            aiAdvice = advice
        )
    }

    fun isRecognitionRecord(meal: RecentMealDto): Boolean {
        val n = meal.notes?.trim().orEmpty()
        val nl = n.lowercase()
        val isPhotoRecognition = nl.startsWith("ai识别") ||
            nl.startsWith("vit模型识别") ||
            nl.startsWith("vit") ||
            nl.startsWith("ocr")
        return isPhotoRecognition
    }

    fun load() {
        scope.launch {
            isLoading = true
            loadError = null
            withContext(Dispatchers.IO) {
                try {
                    val resp = RetrofitClient.getMealApiService()
                        .getRecentMeals(userId, daysWindow).execute()
                    if (!resp.isSuccessful || resp.body()?.isSuccess != true) {
                        loadError = resp.body()?.message ?: "加载失败"
                        dayGroups = emptyList()
                        return@withContext
                    }
                    val meals = resp.body()?.data ?: emptyList()
                    val recognitionMeals = meals.filter { isRecognitionRecord(it) }
                    var records = recognitionMeals.mapNotNull { m ->
                        val ds = m.mealDate ?: return@mapNotNull null
                        runCatching { LocalDate.parse(ds) }.getOrNull()?.let { d -> m.toScanRecord(d) }
                    }
                    when (filterMode) {
                        ScanFilterMode.ALL -> { }
                        ScanFilterMode.WITH_IMAGE -> records = records.filter { it.imageUrl != null }
                        ScanFilterMode.TEXT_ONLY -> records = records.filter { it.imageUrl == null }
                    }
                    val grouped = records
                        .groupBy { it.mealDate }
                        .map { (date, list) ->
                            val label = when (date) {
                                today -> "今天"
                                yesterday -> "昨天"
                                else -> date.format(dateDisplayFormatter)
                            }
                            DayGroup(
                                sortKey = date,
                                label = label,
                                records = list.sortedByDescending { it.timeSortKey }
                            )
                        }
                        .sortedByDescending { it.sortKey }
                    dayGroups = grouped
                } catch (e: Exception) {
                    loadError = e.message ?: "网络异常"
                    dayGroups = emptyList()
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(daysWindow, filterMode) { load() }

    val filteredGroups = remember(query, dayGroups) {
        if (query.isBlank()) dayGroups
        else dayGroups.map { group ->
            group.copy(records = group.records.filter {
                it.name.contains(query, ignoreCase = true)
            })
        }.filter { it.records.isNotEmpty() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Text(
                "历史扫描记录",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray800,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { load() }) {
                Icon(Icons.Default.Refresh, "刷新", tint = MintGreen)
            }
        }

        Text(
            "仅展示拍照识别（ViT/AI）的记录，含识别结果和AI建议",
            fontSize = 11.sp,
            color = Gray400,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(7 to "7天", 30 to "30天", 90 to "90天").forEach { (d, label) ->
                FilterChip(
                    selected = daysWindow == d,
                    onClick = { daysWindow = d },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MintBg,
                        selectedLabelColor = MintGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterMode == ScanFilterMode.ALL,
                onClick = { filterMode = ScanFilterMode.ALL },
                label = { Text("全部", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MintBg,
                    selectedLabelColor = MintGreen
                )
            )
            FilterChip(
                selected = filterMode == ScanFilterMode.WITH_IMAGE,
                onClick = { filterMode = ScanFilterMode.WITH_IMAGE },
                label = { Text("带图", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MintBg,
                    selectedLabelColor = MintGreen
                )
            )
            FilterChip(
                selected = filterMode == ScanFilterMode.TEXT_ONLY,
                onClick = { filterMode = ScanFilterMode.TEXT_ONLY },
                label = { Text("无图", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MintBg,
                    selectedLabelColor = MintGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(20.dp)
                )
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text("搜索食物名称...", fontSize = 14.sp, color = Gray400)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                if (query.isNotBlank()) {
                    IconButton(
                        onClick = { query = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, "清除", tint = Gray400)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else if (loadError != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("加载失败", fontSize = 15.sp, color = RedHigh)
                Text(loadError!!, fontSize = 12.sp, color = Gray500, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { load() }) { Text("重试", color = MintGreen) }
            }
        } else if (filteredGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔍", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    if (query.isNotBlank()) "未找到匹配的记录"
                    else "暂无记录",
                    fontSize = 15.sp,
                    color = Gray700
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (query.isNotBlank()) "试试换个关键词"
                    else "使用拍照识别功能后，识别结果会显示在这里",
                    fontSize = 12.sp,
                    color = Gray400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredGroups.forEach { group ->
                    item(key = "h_${group.sortKey}") {
                        Text(
                            group.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray700,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(group.records, key = { it.mealId ?: "${group.sortKey}_${it.name}_${it.time}".hashCode() }) { record ->
                        ScanRecordCard(record)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

private fun resolveImageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    val p = path.trim()
    // 远程 URL
    if (p.startsWith("http://") || p.startsWith("https://")) return p
    // content:// 或 file:// URI，直接返回让 Coil 处理
    if (p.startsWith("content://") || p.startsWith("file://")) return p
    // Android 本地文件路径（应用私有目录/外部存储）——Coil 可直接加载 file://
    val androidLocalPrefixes = listOf(
        "/data/", "/storage/", "/sdcard/", "/mnt/"
    )
    if (androidLocalPrefixes.any { p.startsWith(it) }) {
        return "file://$p"
    }
    // 其余当作后端相对路径，拼接 API_ORIGIN
    val normalized = if (p.startsWith("/")) p else "/$p"
    return API_ORIGIN.trimEnd('/') + normalized
}

/**
 * 从 notes 字段中解析 AI 建议：
 * - 支持 "AI建议：xxx" / "AI建议: xxx" / "AI: xxx" / 以 "|" 分隔的多段文本
 * - 若未命中关键词但 notes 非空，返回第一段文本（去掉 "手动输入" 之类提示词）
 */
private fun parseAiAdvice(notes: String?): String? {
    if (notes.isNullOrBlank()) return null
    val parts = notes.split("|", "\n").map { it.trim() }.filter { it.isNotBlank() }
    if (parts.isEmpty()) return null

    val keywordMatch = parts.firstOrNull { p ->
        val pl = p.lowercase()
        pl.startsWith("ai建议") ||
            pl.startsWith("ai 建议") ||
            pl.startsWith("ai:") ||
            pl.startsWith("ai：") ||
            pl.contains("建议：") ||
            pl.contains("建议:")
    }
    val raw = keywordMatch
        ?: parts.firstOrNull { !it.matches(Regex("^[\\d\\.]+\\s*(g|kcal|克|千卡).*")) }
        ?: parts.first()

    return raw
        .removePrefix("AI建议：").removePrefix("AI建议:")
        .removePrefix("AI 建议：").removePrefix("AI 建议:")
        .removePrefix("AI:").removePrefix("AI：")
        .replace(Regex("^建议[：:]\\s*"), "")
        .trim()
        .takeIf { it.isNotBlank() && it != "手动输入" }
}

@Composable
private fun ScanRecordCard(record: ScanRecord) {
    val sugarValue = record.sugar.removeSuffix("g").toIntOrNull() ?: 0
    val sugarColor = when {
        sugarValue > 25 -> RedHigh
        sugarValue > 12 -> OrangeMid
        else -> MintGreen
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Gray100
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (record.imageUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(record.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("📷", fontSize = 28.sp)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            record.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray800,
                            modifier = Modifier.weight(1f)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                record.sugar,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = sugarColor
                            )
                            Text(
                                record.calories,
                                fontSize = 11.sp,
                                color = Gray500
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        record.time,
                        fontSize = 11.sp,
                        color = Gray400
                    )
                }
            }

            if (!record.aiAdvice.isNullOrBlank()) {
                var expanded by remember { mutableStateOf(false) }
                val isLong = record.aiAdvice.length > 60
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isLong) Modifier.clickable { expanded = !expanded } else Modifier),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF9FAFB)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            record.aiAdvice,
                            fontSize = 11.sp,
                            color = Color(0xFF666666),
                            lineHeight = 16.sp,
                            maxLines = if (expanded) Int.MAX_VALUE else 2
                        )
                        if (isLong) {
                            Text(
                                if (expanded) "收起 ▲" else "展开 ▼",
                                fontSize = 10.sp,
                                color = MintGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
