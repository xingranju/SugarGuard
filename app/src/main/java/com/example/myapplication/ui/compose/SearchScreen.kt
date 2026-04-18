package com.example.myapplication.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.Drink
import com.example.myapplication.viewmodel.LocalMealViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mealViewModel: LocalMealViewModel = viewModel()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1).toInt()

    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    var allDrinks by remember { mutableStateOf(listOf<Drink>()) }
    var categories by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val drinksResp = RetrofitClient.getDrinkApiService().getAllDrinks().execute()
                if (drinksResp.isSuccessful && drinksResp.body()?.isSuccess == true) {
                    allDrinks = drinksResp.body()?.data ?: emptyList()
                }
                val catResp = RetrofitClient.getDrinkApiService().getAllCategories().execute()
                if (catResp.isSuccessful && catResp.body()?.isSuccess == true) {
                    categories = catResp.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    val filteredDrinks = remember(query, selectedCategory, allDrinks) {
        allDrinks.filter { drink ->
            val matchesQuery = query.isBlank() ||
                    (drink.drinkName ?: "").contains(query, ignoreCase = true) ||
                    (drink.brand ?: "").contains(query, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                    drink.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    val popularDrinks = remember(allDrinks) {
        allDrinks.sortedByDescending { it.healthScore ?: 0 }.take(8)
    }

    var pendingDrink by remember { mutableStateOf<Drink?>(null) }

    fun addDrinkToMeal(drink: Drink) {
        pendingDrink = drink
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Gray400, modifier = Modifier.size(20.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it; isSearching = it.isNotBlank() },
                        placeholder = { Text("搜索饮品名称或品牌...", fontSize = 14.sp, color = Gray400) },
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
                        IconButton(onClick = { query = ""; isSearching = false }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, "清除", tint = Gray400)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isSearching && selectedCategory == null) {
                    item {
                        Text("分类筛选", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { cat ->
                                val catEmoji = when (cat) {
                                    "奶茶" -> "🧋"; "咖啡" -> "☕"; "果汁" -> "🧃"; "茶饮" -> "🍵"
                                    "碳酸饮料" -> "🥤"; "乳制品" -> "🥛"; "功能饮料" -> "⚡"; "气泡水" -> "💧"
                                    else -> "🍹"
                                }
                                Surface(
                                    modifier = Modifier.clickable { selectedCategory = cat },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MintBg
                                ) {
                                    Text(
                                        "$catEmoji $cat",
                                        fontSize = 13.sp, color = MintGreen, fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("热门推荐", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                    }
                    items(popularDrinks) { drink ->
                        DrinkSearchCard(drink) { addDrinkToMeal(it) }
                    }
                } else {
                    if (selectedCategory != null) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.clickable { selectedCategory = null },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MintBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedCategory!!, fontSize = 13.sp, color = MintGreen, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Close, contentDescription = null, tint = MintGreen, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${filteredDrinks.size}款饮品", fontSize = 12.sp, color = Gray400)
                            }
                        }
                    }

                    if (isSearching) {
                        item {
                            Text("搜索结果 (${filteredDrinks.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                        }
                    }

                    if (filteredDrinks.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔍", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("未找到相关饮品", fontSize = 15.sp, color = Gray600)
                                Text("试试换个关键词搜索", fontSize = 12.sp, color = Gray400)
                            }
                        }
                    } else {
                        items(filteredDrinks) { drink ->
                            DrinkSearchCard(drink) { addDrinkToMeal(it) }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    pendingDrink?.let { drink ->
        MealDrinkAddBottomSheet(
            drinkName = drink.drinkName ?: "未知饮品",
            sugar = (drink.sugarContent ?: 0f).toDouble(),
            calories = (drink.calories ?: 0f).toDouble(),
            imageUrl = drink.imageUrl,
            defaultNotes = "${drink.brand ?: ""} ${drink.category ?: ""}".trim(),
            requireExplicitMeal = true,
            servingSize = (drink.volume ?: 500f).toDouble(),
            servingSizeUnit = "ml",
            onDismiss = { pendingDrink = null },
            onSave = { mealType, portionSize, notes, multiplier ->
                scope.launch {
                    mealViewModel.addMeal(
                        userId = userId,
                        foodName = drink.drinkName ?: "未知饮品",
                        sugarContent = (drink.sugarContent ?: 0f).toDouble() * multiplier,
                        calories = (drink.calories ?: 0f).toDouble() * multiplier,
                        protein = drink.protein?.toDouble()?.let { it * multiplier },
                        fat = drink.fat?.toDouble()?.let { it * multiplier },
                        carbohydrate = null,
                        portionSize = portionSize,
                        notes = notes,
                        mealType = mealType,
                        imageUrl = drink.imageUrl
                    )
                    Toast.makeText(context, "已添加「${drink.drinkName}」到日记", Toast.LENGTH_SHORT).show()
                }
                pendingDrink = null
            }
        )
    }
}

@Composable
private fun DrinkSearchCard(
    drink: Drink,
    onAdd: (Drink) -> Unit
) {
    val sugarColor = when {
        (drink.sugarContent ?: 0f) > 25 -> Color(0xFFEF5350)
        (drink.sugarContent ?: 0f) > 12 -> Color(0xFFFF9800)
        else -> MintGreen
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
            if (!drink.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = drink.imageUrl,
                    contentDescription = drink.drinkName,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(12.dp), color = MintBg) {
                    Box(contentAlignment = Alignment.Center) { Text("🥤", fontSize = 22.sp) }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    drink.drinkName ?: "未知饮品",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!drink.brand.isNullOrBlank()) {
                        Text(drink.brand!!, fontSize = 11.sp, color = Gray500)
                        Text("·", fontSize = 11.sp, color = Gray400)
                    }
                    if (!drink.category.isNullOrBlank()) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MintBg) {
                            Text(drink.category!!, fontSize = 10.sp, color = MintGreen,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("含糖 ${drink.sugarContent?.toInt() ?: 0}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = sugarColor)
                    Text("${drink.calories?.toInt() ?: 0}kcal", fontSize = 12.sp, color = Gray500)
                    if ((drink.healthScore ?: 0) > 0) {
                        Text("★${drink.healthScore}", fontSize = 12.sp, color = Color(0xFFFFC107))
                    }
                }
            }

            Surface(
                modifier = Modifier.clickable { onAdd(drink) },
                shape = CircleShape,
                color = MintBg
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = "添加",
                    tint = MintGreen,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
        }
    }
}

