package com.example.myapplication.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.Drink
import com.example.myapplication.model.DrinkPreferenceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DrinkPreferenceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefStore = context.getSharedPreferences("drink_preferences", Context.MODE_PRIVATE)

    val drinkTypes = listOf("奶茶", "咖啡", "果汁", "茶饮", "气泡水", "酸奶", "豆浆", "椰子水")
    val sweetness = listOf("全糖", "七分糖", "三分糖", "无糖", "代糖可接受")
    val dislikes = listOf("含乳糖", "含咖啡因(晚间)", "坚果类", "芋头类")
    val scenes = listOf("日常通勤", "学习提神", "运动前后", "社交聚餐", "深夜加班")

    fun loadSet(key: String, default: Set<Int>): Set<Int> {
        val str = prefStore.getString(key, null) ?: return default
        return str.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    var selectedDrinks by remember { mutableStateOf(loadSet("drinks", setOf(0, 1, 3, 7))) }
    var selectedSweet by remember { mutableStateOf(loadSet("sweetness", setOf(2))) }
    var selectedDislikes by remember { mutableStateOf(loadSet("dislikes", setOf(0, 1))) }
    var selectedScenes by remember { mutableStateOf(loadSet("scenes", setOf(0, 1, 4))) }

    fun savePreferences() {
        prefStore.edit()
            .putString("drinks", selectedDrinks.joinToString(","))
            .putString("sweetness", selectedSweet.joinToString(","))
            .putString("dislikes", selectedDislikes.joinToString(","))
            .putString("scenes", selectedScenes.joinToString(","))
            .apply()
        Toast.makeText(context, "偏好已保存", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
                }
                Text("饮品偏好", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800)
            }
            TextButton(onClick = { savePreferences() }) {
                Text("保存", color = MintGreen, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Text(
                    "设置你的饮品偏好，AI助手会据此给你更精准的推荐。",
                    fontSize = 12.sp, color = Gray600,
                    modifier = Modifier.padding(16.dp)
                )
            }

            FeaturedDrinkSection()

            ChipSection("喜欢的饮品类型", drinkTypes, selectedDrinks, ChipStyle.ACTIVE) { selectedDrinks = it }
            ChipSection("甜度偏好", sweetness, selectedSweet, ChipStyle.ACTIVE) { selectedSweet = it }
            ChipSection("不喜欢/过敏", dislikes, selectedDislikes, ChipStyle.DISLIKE) { selectedDislikes = it }
            ChipSection("控糖场景", scenes, selectedScenes, ChipStyle.ACTIVE) { selectedScenes = it }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MintBg) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI推荐将基于以上偏好", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                    Text(
                        "例如：你标注了\"三分糖\"偏好，AI会优先推荐三分糖或以下的饮品选项。",
                        fontSize = 10.sp, color = Gray600, modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            FavoriteDrinksSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

enum class ChipStyle { ACTIVE, DISLIKE }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(
    title: String,
    items: List<String>,
    selected: Set<Int>,
    style: ChipStyle,
    onSelectionChange: (Set<Int>) -> Unit
) {
    Column {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEachIndexed { index, label ->
                val isSelected = selected.contains(index)
                val bgColor = when {
                    isSelected && style == ChipStyle.ACTIVE -> MintGreen
                    isSelected && style == ChipStyle.DISLIKE -> Color(0xFFFEF2F2)
                    else -> Gray100
                }
                val textColor = when {
                    isSelected && style == ChipStyle.ACTIVE -> Color.White
                    isSelected && style == ChipStyle.DISLIKE -> Color(0xFFEF4444)
                    else -> Color(0xFF666666)
                }
                val borderColor = when {
                    isSelected && style == ChipStyle.DISLIKE -> Color(0xFFFECACA)
                    !isSelected -> Color(0xFFE5E7EB)
                    else -> Color.Transparent
                }

                Surface(
                    modifier = Modifier
                        .clickable {
                            onSelectionChange(if (isSelected) selected - index else selected + index)
                        }
                        .then(
                            if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor, RoundedCornerShape(20.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = bgColor
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedDrinkSection() {
    val context = LocalContext.current
    val prefStore = context.getSharedPreferences("drink_preferences", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()

    var allDrinks by remember { mutableStateOf(listOf<Drink>()) }
    var featuredDrinkIds by remember { mutableStateOf(setOf<Int>()) }
    var showDrinkPicker by remember { mutableStateOf(false) }
    var expandedDrinkId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val drinksResp = RetrofitClient.getDrinkApiService().getAllDrinks().execute()
                if (drinksResp.isSuccessful && drinksResp.body()?.isSuccess == true) {
                    allDrinks = drinksResp.body()?.data ?: emptyList()
                }
                val savedIds = prefStore.getString("featured_drink_ids", null)
                if (!savedIds.isNullOrBlank()) {
                    featuredDrinkIds = savedIds.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                }
                if (featuredDrinkIds.isEmpty()) {
                    val prefResp = RetrofitClient.getDrinkPreferenceApiService().getUserPreferences().execute()
                    if (prefResp.isSuccessful && prefResp.body()?.isSuccess == true) {
                        val highScorePrefs = prefResp.body()?.data
                            ?.filter { (it.preferenceScore ?: 0) >= 4 }
                            ?.map { it.drinkId }
                            ?.toSet() ?: emptySet()
                        if (highScorePrefs.isNotEmpty()) {
                            featuredDrinkIds = highScorePrefs
                        }
                    }
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    val featuredDrinks = allDrinks.filter { featuredDrinkIds.contains(it.drinkId) }

    fun saveFeaturedIds(ids: Set<Int>) {
        featuredDrinkIds = ids
        prefStore.edit().putString("featured_drink_ids", ids.joinToString(",")).apply()
    }

    fun addFeaturedDrink(drink: Drink) {
        val newIds = featuredDrinkIds + drink.drinkId
        saveFeaturedIds(newIds)
        scope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.getDrinkPreferenceApiService()
                    .addOrUpdatePreference(DrinkPreferenceRequest(drink.drinkId, 5)).execute()
            } catch (_: Exception) {}
        }
        Toast.makeText(context, "已添加偏好饮品：${drink.drinkName}", Toast.LENGTH_SHORT).show()
    }

    fun removeFeaturedDrink(drink: Drink) {
        val newIds = featuredDrinkIds - drink.drinkId
        saveFeaturedIds(newIds)
        if (expandedDrinkId == drink.drinkId) expandedDrinkId = null
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("我的偏好饮品", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                if (featuredDrinks.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = MintBg) {
                        Text(
                            "${featuredDrinks.size}款",
                            fontSize = 10.sp, color = MintGreen, fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            TextButton(onClick = { showDrinkPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加", fontSize = 12.sp, color = MintGreen)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MintGreen, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        } else if (featuredDrinks.isNotEmpty()) {
            featuredDrinks.forEach { drink ->
                val isExpanded = expandedDrinkId == drink.drinkId
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedDrinkId = if (isExpanded) null else drink.drinkId }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!drink.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = drink.imageUrl,
                                    contentDescription = drink.drinkName,
                                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(10.dp), color = MintBg) {
                                    Box(contentAlignment = Alignment.Center) { Text("🥤", fontSize = 22.sp) }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(drink.drinkName ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                                Text(
                                    "${drink.brand ?: ""} · ${drink.category ?: ""}",
                                    fontSize = 11.sp, color = Gray500
                                )
                                Text(
                                    "${drink.sugarContent?.toInt() ?: 0}g糖 · ${drink.calories?.toInt() ?: 0}kcal",
                                    fontSize = 11.sp, color = Gray400
                                )
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = MintBg) {
                                Text(
                                    "偏好",
                                    fontSize = 10.sp, color = MintGreen, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (isExpanded) {
                            Divider(color = Color(0xFFF3F4F6))
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    DrinkStatItem("含糖", "${drink.sugarContent?.toInt() ?: 0}g", Color(0xFFFF9800))
                                    DrinkStatItem("热量", "${drink.calories?.toInt() ?: 0}kcal", Color(0xFFEF5350))
                                    DrinkStatItem("健康分", "${drink.healthScore ?: 0}", MintGreen)
                                    DrinkStatItem("咖啡因", "${drink.caffeine?.toInt() ?: 0}mg", Color(0xFF8D6E63))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { removeFeaturedDrink(drink) }) {
                                        Text("移除偏好", fontSize = 12.sp, color = RedHigh)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDrinkPicker = true },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = CircleShape, color = MintBg, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MintGreen, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("选择你喜欢的饮品", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray600)
                    Text("可选多款，从数据库中挑选", fontSize = 11.sp, color = Gray400, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }

    if (showDrinkPicker) {
        var pendingIds by remember { mutableStateOf(featuredDrinkIds) }
        val filteredDrinks = if (searchQuery.isBlank()) allDrinks
        else allDrinks.filter {
            (it.drinkName ?: "").contains(searchQuery, ignoreCase = true) ||
                    (it.brand ?: "").contains(searchQuery, ignoreCase = true) ||
                    (it.category ?: "").contains(searchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showDrinkPicker = false; searchQuery = "" },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("选择偏好饮品", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (pendingIds.isNotEmpty()) {
                            Text("已选${pendingIds.size}款", fontSize = 12.sp, color = MintGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索饮品名称/品牌...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, cursorColor = MintGreen)
                    )
                }
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filteredDrinks) { drink ->
                        val isSelected = pendingIds.contains(drink.drinkId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pendingIds = if (isSelected) pendingIds - drink.drinkId else pendingIds + drink.drinkId
                                }
                                .background(if (isSelected) MintBg else Color.Transparent, RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!drink.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = drink.imageUrl,
                                    contentDescription = drink.drinkName,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = Gray100) {
                                    Box(contentAlignment = Alignment.Center) { Text("🥤", fontSize = 18.sp) }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    drink.drinkName ?: "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Gray800,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${drink.brand ?: ""} · ${drink.sugarContent?.toInt() ?: 0}g糖 · ${drink.calories?.toInt() ?: 0}kcal",
                                    fontSize = 11.sp,
                                    color = Gray400
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "已选择", tint = MintGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (drink != filteredDrinks.lastOrNull()) {
                            Divider(color = Color(0xFFF5F5F5))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val addedIds = pendingIds - featuredDrinkIds
                    saveFeaturedIds(pendingIds)
                    for (id in addedIds) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                RetrofitClient.getDrinkPreferenceApiService()
                                    .addOrUpdatePreference(DrinkPreferenceRequest(id, 5)).execute()
                            } catch (_: Exception) {}
                        }
                    }
                    if (addedIds.isNotEmpty()) {
                        Toast.makeText(context, "已添加${addedIds.size}款偏好饮品", Toast.LENGTH_SHORT).show()
                    }
                    showDrinkPicker = false
                    searchQuery = ""
                }) {
                    Text("确定", color = MintGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDrinkPicker = false; searchQuery = "" }) {
                    Text("取消", color = Gray500)
                }
            }
        )
    }
}

@Composable
private fun DrinkStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = Gray500, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun FavoriteDrinksSection() {
    val scope = rememberCoroutineScope()
    var allDrinks by remember { mutableStateOf(listOf<Drink>()) }
    var favoriteIds by remember { mutableStateOf(setOf<Int>()) }
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val prefResp = RetrofitClient.getDrinkPreferenceApiService().getUserPreferences().execute()
                    if (prefResp.isSuccessful && prefResp.body()?.isSuccess == true) {
                        favoriteIds = (prefResp.body()?.data ?: emptyList()).map { it.drinkId }.toSet()
                    }
                    val drinksResp = RetrofitClient.getDrinkApiService().getAllDrinks().execute()
                    if (drinksResp.isSuccessful && drinksResp.body()?.isSuccess == true) {
                        allDrinks = drinksResp.body()?.data ?: emptyList()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val favDrinks = allDrinks.filter { favoriteIds.contains(it.drinkId) }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("收藏的饮品", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
            TextButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加", fontSize = 12.sp, color = MintGreen)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (favDrinks.isEmpty()) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
                Text("暂无收藏饮品，点击添加按钮选择", fontSize = 12.sp, color = Gray400,
                    modifier = Modifier.padding(24.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            favDrinks.forEach { drink ->
                Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!drink.imageUrl.isNullOrBlank()) {
                            AsyncImage(model = drink.imageUrl, contentDescription = drink.drinkName,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        } else {
                            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(8.dp), color = MintBg) {
                                Box(contentAlignment = Alignment.Center) { Text("🥤", fontSize = 20.sp) }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(drink.drinkName ?: "", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray800)
                            Text("${drink.brand ?: ""} · ${drink.sugarContent?.toInt() ?: 0}g糖", fontSize = 11.sp, color = Gray400)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    try { RetrofitClient.getDrinkPreferenceApiService().deletePreference(drink.drinkId).execute() } catch (_: Exception) {}
                                }
                                favoriteIds = favoriteIds - drink.drinkId
                            }
                        }) {
                            Icon(Icons.Default.Favorite, contentDescription = "取消收藏", tint = RedHigh, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("选择饮品", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    allDrinks.forEach { drink ->
                        val isFav = favoriteIds.contains(drink.drinkId)
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            if (isFav) {
                                                RetrofitClient.getDrinkPreferenceApiService().deletePreference(drink.drinkId).execute()
                                            } else {
                                                RetrofitClient.getDrinkPreferenceApiService()
                                                    .addOrUpdatePreference(DrinkPreferenceRequest(drink.drinkId, 5)).execute()
                                            }
                                        } catch (_: Exception) {}
                                    }
                                    favoriteIds = if (isFav) favoriteIds - drink.drinkId else favoriteIds + drink.drinkId
                                }
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFav) RedHigh else Gray300,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(drink.drinkName ?: "", fontSize = 14.sp, color = Gray800)
                                Text("${drink.brand ?: ""} · ${drink.sugarContent?.toInt() ?: 0}g", fontSize = 11.sp, color = Gray400)
                            }
                        }
                        Divider(color = Color(0xFFF5F5F5))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text("完成", color = MintGreen) }
            }
        )
    }
}
