package com.example.myapplication.ui.compose

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.model.MealRecord
import com.example.myapplication.viewmodel.AIServiceViewModel
import com.example.myapplication.viewmodel.LocalMealViewModel
import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@Composable
fun DiaryScreen(
    viewModel: AIServiceViewModel,
    mealViewModel: LocalMealViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToCamera: () -> Unit = {},
    onNavigateToSearch: (() -> Unit)? = null,
    onNavigateToChat: (() -> Unit)? = null
) {
    var showAddDrinkScreen by remember { mutableStateOf(false) }
    var showManualAddScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1).toInt()

    if (showAddDrinkScreen) {
        AddDrinkRecordScreen(
            onNavigateBack = {
                showAddDrinkScreen = false
                mealViewModel.getDailyMeals(userId, LocalDate.now())
            }
        )
        return
    }

    if (showManualAddScreen) {
        ManualAddMealScreen(
            userId = userId,
            mealViewModel = mealViewModel,
            onBack = {
                showManualAddScreen = false
                mealViewModel.getDailyMeals(userId, mealViewModel.selectedDate.value ?: LocalDate.now())
            }
        )
        return
    }

    val selectedDate by mealViewModel.selectedDate.observeAsState(LocalDate.now())
    val dailyMeals by mealViewModel.dailyMeals.observeAsState(emptyList())
    val isLoading by mealViewModel.isLoading.observeAsState(false)
    val dailySugarTotal by mealViewModel.dailySugarTotal.observeAsState(0.0)

    LaunchedEffect(selectedDate) {
        mealViewModel.getDailyMeals(userId, selectedDate)
    }

    val today = LocalDate.now()
    val baseDate = selectedDate
    val weekDates = (6 downTo 0).map { baseDate.minusDays(it.toLong()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val cal = Calendar.getInstance().apply {
            set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
        }
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val picked = LocalDate.of(year, month + 1, day)
                mealViewModel.selectDate(picked, userId)
                showDatePicker = false
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
        }.show()
        showDatePicker = false
    }

    var sugarLimit by remember { mutableFloatStateOf(25f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val resp = com.example.myapplication.api.RetrofitClient.getUserProfileApiService().getHealthProfile().execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    sugarLimit = resp.body()?.data?.sugarLimit ?: 25f
                }
            } catch (_: Exception) {}
        }
    }
    val score = (100 - (dailySugarTotal / sugarLimit * 30).toInt()).coerceIn(0, 100)
    val breakfastSugar = dailyMeals.filter { it.mealType == "breakfast" }.sumOf { it.sugarContent }
    val lunchSugar = dailyMeals.filter { it.mealType == "lunch" }.sumOf { it.sugarContent }
    val dinnerSugar = dailyMeals.filter { it.mealType == "dinner" }.sumOf { it.sugarContent }
    val snackSugar = dailyMeals.filter { it.mealType == "snack" }.sumOf { it.sugarContent }

    Box(modifier = Modifier.fillMaxSize()) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(40.dp))
                Text(
                    "饮食日记",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "选择日期",
                        tint = MintGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal date selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(weekDates) { _, date ->
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    Surface(
                        modifier = Modifier
                            .width(50.dp)
                            .clickable { mealViewModel.selectDate(date, userId) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MintGreen else Color(0xFFF5F5F5)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isToday && isSelected) "今天"
                                else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFFBDBDBD)
                            )
                            Text(
                                text = "${date.dayOfMonth}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF757575)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Summary card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "${dailySugarTotal.toInt()}g",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    " / 目标 ${sugarLimit.toInt()}g",
                                    fontSize = 14.sp,
                                    color = Color(0xFFBDBDBD)
                                )
                            }
                            val scoreColor = when {
                                score >= 90 -> Color(0xFF2E7D32)
                                score >= 70 -> MintGreen
                                score >= 50 -> Color(0xFFFFA726)
                                score >= 30 -> Color(0xFFEF6C00)
                                else -> Color(0xFFE53935)
                            }
                            val scoreBg = when {
                                score >= 90 -> Color(0xFFE8F5E9)
                                score >= 70 -> MintBg
                                score >= 50 -> Color(0xFFFFF3E0)
                                score >= 30 -> Color(0xFFFBE9E7)
                                else -> Color(0xFFFFEBEE)
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = scoreBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("评分", fontSize = 10.sp, color = Color(0xFFBDBDBD))
                                    Text("$score", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = scoreColor)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val progress = (dailySugarTotal / sugarLimit).toFloat().coerceIn(0f, 1f)
                        val progressColor = when {
                            score >= 90 -> Color(0xFF2E7D32)
                            score >= 70 -> MintGreen
                            score >= 50 -> Color(0xFFFFA726)
                            score >= 30 -> Color(0xFFEF6C00)
                            else -> Color(0xFFE53935)
                        }
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = Color(0xFFF5F5F5),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MealSummaryCol("早餐", "${breakfastSugar.toInt()}g")
                            MealSummaryCol("午餐", "${lunchSugar.toInt()}g")
                            MealSummaryCol("晚餐", if (dinnerSugar > 0) "${dinnerSugar.toInt()}g" else "—")
                            MealSummaryCol("加餐", "${snackSugar.toInt()}g")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "今日记录",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF555555)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MintGreen)
                    }
                } else if (dailyMeals.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📝", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("暂无记录", fontSize = 14.sp, color = Color(0xFFBDBDBD))
                            Text("点击 + 按钮开始记录", fontSize = 12.sp, color = Color(0xFFD0D0D0))
                        }
                    }
                } else {
                    val mealTypeOrder = listOf("breakfast", "lunch", "dinner", "snack")
                    val sortedMeals = dailyMeals.sortedWith(
                        compareBy<MealRecord> { mealTypeOrder.indexOf(it.mealType).let { idx -> if (idx < 0) 99 else idx } }
                            .thenBy { it.mealTime }
                    )
                    var lastMealType = ""
                    var isFirstGroup = true
                    sortedMeals.forEach { meal ->
                        val mealLabel = getMealTimeLabel(meal.mealType)
                        if (mealLabel != lastMealType) {
                            lastMealType = mealLabel
                            Text(
                                mealLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFBDBDBD),
                                modifier = Modifier.padding(top = if (!isFirstGroup) 16.dp else 0.dp, bottom = 8.dp)
                            )
                            isFirstGroup = false
                        }
                        DiaryFoodItem(
                            meal = meal,
                            onDelete = { mealViewModel.deleteMeal(userId, meal.mealId ?: 0) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        onNavigateToChat?.let { navigateToChat ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 24.dp)
                    .size(56.dp)
                    .clickable { navigateToChat() },
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF26A69A), Color(0xFF00897B))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 22.sp)
                }
            }
        }

        var fabExpanded by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.animation.AnimatedVisibility(visible = fabExpanded) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.clickable {
                            fabExpanded = false
                            onNavigateToSearch?.invoke() ?: run { showAddDrinkScreen = true }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Search, "搜索饮品", tint = MintGreen, modifier = Modifier.size(16.dp))
                            Text("饮品搜索", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        }
                    }
                    Surface(
                        modifier = Modifier.clickable {
                            fabExpanded = false
                            showManualAddScreen = true
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, "手动", tint = MintGreen, modifier = Modifier.size(16.dp))
                            Text("手动添加", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        }
                    }
                    Surface(
                        modifier = Modifier.clickable {
                            fabExpanded = false
                            onNavigateToCamera()
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, "拍照", tint = MintGreen, modifier = Modifier.size(16.dp))
                            Text("拍照识别", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = MintGreen,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Add, "添加记录", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun MealSummaryCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color(0xFFBDBDBD))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
    }
}

@Composable
private fun DiaryFoodItem(meal: MealRecord, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var notesExpanded by remember { mutableStateOf(false) }

    val sugarColor = when {
        meal.sugarContent > 20 -> Color(0xFFEF5350)
        meal.sugarContent > 10 -> Color(0xFFFF9800)
        else -> MintGreen
    }

    val hasLongNotes = (meal.notes?.length ?: 0) > 30

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
                if (!meal.foodImagePath.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(com.example.myapplication.util.resolveMealImageData(meal.foodImagePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = meal.foodName,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(getMealTypeEmoji(meal.mealType), fontSize = 24.sp)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            meal.foodName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${meal.sugarContent.toInt()}g",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = sugarColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            meal.mealTime.take(5),
                            fontSize = 10.sp,
                            color = Color(0xFFD0D0D0)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MintBg
                        ) {
                            Text(
                                getMealTypeName(meal.mealType),
                                fontSize = 9.sp,
                                color = MintGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        tint = Color(0xFFD0D0D0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!meal.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (hasLongNotes) notesExpanded = !notesExpanded },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF9FAFB)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            meal.notes,
                            fontSize = 11.sp,
                            color = Color(0xFF666666),
                            lineHeight = 16.sp,
                            maxLines = if (notesExpanded) Int.MAX_VALUE else 2,
                        )
                        if (hasLongNotes) {
                            Text(
                                if (notesExpanded) "收起 ▲" else "展开 ▼",
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗?") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}

private fun getMealTimeLabel(mealType: String): String {
    return when (mealType) {
        "breakfast" -> "早上 · 早餐"
        "lunch" -> "中午 · 午餐"
        "dinner" -> "晚上 · 晚餐"
        "snack" -> "下午 · 加餐"
        else -> "其他"
    }
}

fun getMealTypeEmoji(mealType: String): String {
    return when (mealType) {
        "breakfast" -> "🌅"
        "lunch" -> "🌞"
        "dinner" -> "🌙"
        "snack" -> "🍎"
        else -> "🍽️"
    }
}

fun getMealTypeName(mealType: String): String {
    return when (mealType) {
        "breakfast" -> "早餐"
        "lunch" -> "午餐"
        "dinner" -> "晚餐"
        "snack" -> "加餐"
        else -> "其他"
    }
}

@Composable
private fun ManualAddMealScreen(
    userId: Int,
    mealViewModel: LocalMealViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var foodName by remember { mutableStateOf("") }
    var sugarContent by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableIntStateOf(0) }
    var portionSize by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val mealTypes = listOf("breakfast" to "早餐", "lunch" to "午餐", "dinner" to "晚餐", "snack" to "加餐")

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回", tint = Gray600) }
            Spacer(modifier = Modifier.weight(1f))
            Text("手动添加", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image selection
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("食物图片（可选）", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "食物图片",
                                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                                    .background(Color(0x80000000), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, "移除", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                                .clickable { imagePicker.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F5F5),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0E0E0))
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, "添加图片", tint = Gray400, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("点击选择图片", fontSize = 12.sp, color = Gray400)
                            }
                        }
                    }
                }
            }

            // Food name
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("食物名称 *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = sugarContent,
                            onValueChange = { sugarContent = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("含糖量(g) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                        )
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("热量(kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = portionSize,
                        onValueChange = { portionSize = it },
                        label = { Text("份量（如：一碗、200g）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                    )
                }
            }

            // Meal type
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("餐次", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mealTypes.forEachIndexed { index, (_, label) ->
                            val isSelected = selectedMealType == index
                            Surface(
                                modifier = Modifier.weight(1f).clickable { selectedMealType = index },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MintBg else Color(0xFFF5F5F5),
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))
                                ) else null
                            ) {
                                Text(
                                    label, fontSize = 12.sp,
                                    color = if (isSelected) MintGreen else Gray600,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Notes
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("备注（可选）") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Button(
                onClick = {
                    if (foodName.isBlank()) {
                        Toast.makeText(context, "请输入食物名称", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val sugar = sugarContent.toDoubleOrNull()
                    if (sugar == null) {
                        Toast.makeText(context, "请输入含糖量", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    val imageUrl = selectedImageUri?.let { uri ->
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val dir = File(context.filesDir, "meal_images")
                            dir.mkdirs()
                            val file = File(dir, "meal_${System.currentTimeMillis()}.jpg")
                            inputStream?.use { inp -> file.outputStream().use { out -> inp.copyTo(out) } }
                            file.absolutePath
                        } catch (_: Exception) { null }
                    }
                    mealViewModel.addMeal(
                        userId = userId,
                        foodName = foodName.trim(),
                        sugarContent = sugar,
                        calories = calories.toDoubleOrNull() ?: 0.0,
                        protein = null,
                        fat = null,
                        carbohydrate = null,
                        portionSize = portionSize.trim().ifBlank { null },
                        notes = notes.trim().ifBlank { null },
                        mealType = mealTypes[selectedMealType].first,
                        imageUrl = imageUrl
                    )
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("添加记录", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
