package com.example.myapplication.ui.compose

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.MealRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToRecognition: () -> Unit = {},
    onNavigateToDiary: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)
    val username = prefs.getString("username", "用户") ?: "用户"

    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var todaySugar by remember { mutableDoubleStateOf(0.0) }
    var sugarLimit by remember { mutableFloatStateOf(25f) }
    var breakfastSugar by remember { mutableDoubleStateOf(0.0) }
    var lunchSugar by remember { mutableDoubleStateOf(0.0) }
    var dinnerSugar by remember { mutableDoubleStateOf(0.0) }
    var snackSugar by remember { mutableDoubleStateOf(0.0) }

    data class FoodRecord(val name: String, val sugar: Double, val time: String, val tag: String, val imageUrl: String? = null)
    var recentRecords by remember { mutableStateOf(listOf<FoodRecord>()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var unreadNotifCount by remember { mutableIntStateOf(0) }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    LaunchedEffect(refreshTrigger) {
        try {
            val userInfoApi = RetrofitClient.getUserInfoApiService()
            val userInfo = withContext(Dispatchers.IO) {
                try {
                    val resp = userInfoApi.getUserInfo().execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
                } catch (_: Exception) {
                    null
                }
            }
            avatarUrl = userInfo?.avatarUrl

            val mealApi = RetrofitClient.getMealApiService()
            val mealData = withContext(Dispatchers.IO) {
                try {
                    val resp = mealApi.getDailyMeals(userId.toInt(), today).execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
                } catch (_: Exception) {
                    null
                }
            }
            if (mealData != null) {
                val meals = parseMealsFromResponse(mealData, userId.toInt())
                todaySugar = meals.sumOf { it.sugarContent }
                breakfastSugar = meals.filter { it.mealType.equals("breakfast", true) || it.mealType == "BREAKFAST" }.sumOf { it.sugarContent }
                lunchSugar = meals.filter { it.mealType.equals("lunch", true) || it.mealType == "LUNCH" }.sumOf { it.sugarContent }
                dinnerSugar = meals.filter { it.mealType.equals("dinner", true) || it.mealType == "DINNER" }.sumOf { it.sugarContent }
                snackSugar = meals.filter { it.mealType.equals("snack", true) || it.mealType == "SNACK" }.sumOf { it.sugarContent }
                recentRecords = meals.take(3).map {
                    FoodRecord(
                        it.foodName,
                        it.sugarContent,
                        it.mealTime,
                        it.mealType,
                        it.foodImagePath
                    )
                }
            }

            val profileApi = RetrofitClient.getUserProfileApiService()
            val profile = withContext(Dispatchers.IO) {
                try {
                    val resp = profileApi.getHealthProfile().execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
                } catch (_: Exception) {
                    null
                }
            }
            sugarLimit = profile?.sugarLimit ?: 25f

            val notifApi = RetrofitClient.getNotificationApiService()
            val notifCount = withContext(Dispatchers.IO) {
                try {
                    val resp = notifApi.getUnreadCount(userId).execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data?.toInt() ?: 0 else 0
                } catch (_: Exception) { 0 }
            }
            unreadNotifCount = notifCount
        } catch (_: Exception) {
        }
    }

    val sugarProgress = if (sugarLimit > 0) (todaySugar / sugarLimit).toFloat().coerceIn(0f, 1.5f) else 0f
    val remaining = (sugarLimit - todaySugar.toFloat()).coerceAtLeast(0f)
    val score = (100 - (todaySugar / sugarLimit * 30).toInt()).coerceIn(0, 100)

    val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日 | E", Locale.CHINESE))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top greeting bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToProfile() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = avatarUrl,
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MintBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = username.firstOrNull()?.toString() ?: "U",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                    }
                }

                Column {
                    Text(
                        text = "${getGreeting()}，$username",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = Color(0xFFBDBDBD)
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { onNavigateToNotifications() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "通知",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (unreadNotifCount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp),
                        shape = CircleShape,
                        color = Color(0xFFEF5350)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text(
                                text = if (unreadNotifCount > 99) "99+" else "$unreadNotifCount",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
            // Core data card with ring
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(176.dp)
                    ) {
                        SugarRingProgress(
                            progress = sugarProgress.coerceAtMost(1f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${todaySugar.toInt()}",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    "g",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBDBDBD),
                                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                )
                            }
                            Text(
                                "今日已摄入",
                                fontSize = 10.sp,
                                color = Color(0xFFBDBDBD),
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MintBg
                    ) {
                        Text(
                            text = "剩余额度：${remaining.toInt()}g",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFAFAFA)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("今日评分", fontSize = 10.sp, color = Color(0xFFBDBDBD))
                                Text("$score", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MintGreen)
                                Text("分", fontSize = 9.sp, color = Color(0xFFD0D0D0))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = Color(0xFFF5F5F5))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MealColumn("早餐", "${breakfastSugar.toInt()}g")
                        MealColumn("午餐", "${lunchSugar.toInt()}g")
                        MealColumn("晚餐", if (dinnerSugar > 0) "${dinnerSugar.toInt()}g" else "—")
                        MealColumn("加餐", "${snackSugar.toInt()}g")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Today's intake list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "今日摄入明细",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    "全部记录 >",
                    fontSize = 12.sp,
                    color = MintGreen,
                    modifier = Modifier.clickable { onNavigateToDiary() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recentRecords.isEmpty()) {
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
                        Text("点击底部扫描按钮开始记录", fontSize = 12.sp, color = Color(0xFFD0D0D0))
                    }
                }
            } else {
                recentRecords.forEach { record ->
                    FoodRecordItem(
                        name = record.name,
                        sugar = record.sugar,
                        time = record.time,
                        tag = record.tag,
                        imageUrl = record.imageUrl
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    } // end Column

        // AI 悬浮助手按钮
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .clickable { onNavigateToChat() },
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF26A69A), Color(0xFF00897B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun MealColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color(0xFFBDBDBD))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
    }
}

@Composable
private fun FoodRecordItem(name: String, sugar: Double, time: String, tag: String, imageUrl: String? = null) {
    val sugarColor = when {
        sugar > 20 -> Color(0xFFEF5350)
        sugar > 10 -> Color(0xFFFF9800)
        else -> MintGreen
    }

    val tagLabel = when (tag) {
        "breakfast" -> "早餐"
        "lunch" -> "午餐"
        "dinner" -> "晚餐"
        "snack" -> "加餐"
        else -> tag
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val resolvedImage = com.example.myapplication.util.resolveMealImageData(imageUrl)
            if (resolvedImage != null) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(resolvedImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.toString() ?: "\uD83C\uDF7D",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${sugar.toInt()}g",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = sugarColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tagLabel.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MintBg
                        ) {
                            Text(
                                tagLabel,
                                fontSize = 9.sp,
                                color = MintGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (time.isNotEmpty()) {
                        Text(time, fontSize = 10.sp, color = Color(0xFFD0D0D0))
                    }
                }
            }
        }
    }
}

@Composable
fun SugarRingProgress(progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    val bgColor = Color(0xFFF3F4F6)
    val progressColor = when {
        progress > 1f -> Color(0xFFE53935)
        progress > 0.8f -> Color(0xFFFF9800)
        else -> MintGreen
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - radius * 2) / 2,
            (size.height - radius * 2) / 2
        )
        drawArc(
            color = bgColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun parseMealsFromResponse(data: Map<String, Any>, userId: Int): List<MealRecord> {
    val mealsObj = data["meals"] as? List<*> ?: return emptyList()
    return mealsObj.mapNotNull { item ->
        val map = item as? Map<String, Any?> ?: return@mapNotNull null
        MealRecord(
            mealId = (map["mealId"] as? Number ?: map["meal_id"] as? Number)?.toInt(),
            userId = (map["userId"] as? Number ?: map["user_id"] as? Number)?.toInt() ?: userId,
            mealDate = map["mealDate"] as? String ?: map["meal_date"] as? String ?: "",
            mealTime = map["mealTime"] as? String ?: map["meal_time"] as? String ?: "",
            mealType = map["mealType"] as? String ?: map["meal_type"] as? String ?: "snack",
            drinkId = (map["drinkId"] as? Number ?: map["drink_id"] as? Number)?.toInt(),
            foodName = map["foodName"] as? String ?: map["food_name"] as? String ?: "unknown",
            foodImagePath = map["imagePath"] as? String ?: map["image_path"] as? String,
            sugarContent = (map["sugarContent"] as? Number ?: map["sugar_content"] as? Number)?.toDouble() ?: 0.0,
            calories = (map["calories"] as? Number)?.toDouble() ?: 0.0,
            protein = (map["protein"] as? Number)?.toDouble(),
            fat = (map["fat"] as? Number)?.toDouble(),
            carbohydrate = (map["carbohydrate"] as? Number)?.toDouble(),
            portionSize = map["portion_size"] as? String,
            notes = map["notes"] as? String,
            createdAt = map["created_at"] as? String
        )
    }
}

fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 6 -> "凌晨好"
        hour < 9 -> "早上好"
        hour < 12 -> "上午好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        hour < 22 -> "晚上好"
        else -> "夜深了"
    }
}
