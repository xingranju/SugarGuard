package com.example.myapplication.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import coil.request.ImageRequest
import com.example.myapplication.model.Drink
import com.example.myapplication.util.UserManager
import com.example.myapplication.viewmodel.DrinkViewModel

/**
 * 手动添加饮品记录界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrinkRecordScreen(
    onNavigateBack: () -> Unit,
    viewModel: DrinkViewModel = viewModel()
) {
    val context = LocalContext.current
    val userId = UserManager.getInstance(context).getCurrentUserId()
    
    val drinks by viewModel.drinks.observeAsState(emptyList())
    val brands by viewModel.brands.observeAsState(emptyList())
    val categories by viewModel.categories.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()
    val selectedDrink by viewModel.selectedDrink.observeAsState()
    
    // 搜索和筛选状态
    var searchKeyword by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    
    // 显示饮品详情对话框
    var showDrinkDetailDialog by remember { mutableStateOf(false) }
    
    // 初始化数据
    LaunchedEffect(Unit) {
        viewModel.getAllDrinks()
        viewModel.getAllBrands()
        viewModel.getAllCategories()
    }
    
    // 处理成功消息
    LaunchedEffect(successMessage) {
        successMessage?.let {
            // 显示成功提示后返回
            kotlinx.coroutines.delay(1000)
            viewModel.clearSuccessMessage()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加饮品记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            "筛选"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { 
                    searchKeyword = it
                    viewModel.searchDrinks(keyword = it.ifEmpty { null }, brand = selectedBrand, category = selectedCategory)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索饮品名称...") },
                leadingIcon = { Icon(Icons.Default.Search, "搜索") },
                trailingIcon = {
                    if (searchKeyword.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchKeyword = ""
                            viewModel.searchDrinks(brand = selectedBrand, category = selectedCategory)
                        }) {
                            Icon(Icons.Default.Clear, "清除")
                        }
                    }
                },
                singleLine = true
            )
            
            // 筛选器（品牌和类别）
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("品牌筛选", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 品牌选择器
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 150.dp)
                        ) {
                            item {
                                MyFilterChip(
                                    selected = selectedBrand == null,
                                    onClick = {
                                        selectedBrand = null
                                        viewModel.searchDrinks(keyword = searchKeyword.ifEmpty { null }, category = selectedCategory)
                                    },
                                    label = "全部品牌",
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            items(brands) { brand ->
                                MyFilterChip(
                                    selected = selectedBrand == brand,
                                    onClick = {
                                        selectedBrand = brand
                                        viewModel.searchDrinks(keyword = searchKeyword.ifEmpty { null }, brand = brand, category = selectedCategory)
                                    },
                                    label = brand,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("类别筛选", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 类别选择器
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 150.dp)
                        ) {
                            item {
                                MyFilterChip(
                                    selected = selectedCategory == null,
                                    onClick = {
                                        selectedCategory = null
                                        viewModel.searchDrinks(keyword = searchKeyword.ifEmpty { null }, brand = selectedBrand)
                                    },
                                    label = "全部类别",
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            items(categories) { category ->
                                MyFilterChip(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        selectedCategory = category
                                        viewModel.searchDrinks(keyword = searchKeyword.ifEmpty { null }, brand = selectedBrand, category = category)
                                    },
                                    label = category,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // 饮品列表
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (drinks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "未找到饮品",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(drinks) { drink ->
                        DrinkItemCard(
                            drink = drink,
                            onClick = {
                                viewModel.selectDrink(drink)
                                showDrinkDetailDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // 错误提示
        errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearErrorMessage() },
                title = { Text("错误") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearErrorMessage() }) {
                        Text("确定")
                    }
                }
            )
        }
    }
    
    // 饮品详情和添加对话框
    if (showDrinkDetailDialog && selectedDrink != null) {
        AddDrinkRecordDialog(
            drink = selectedDrink!!,
            userId = userId,
            viewModel = viewModel,
            onDismiss = {
                showDrinkDetailDialog = false
                viewModel.selectDrink(null)
            }
        )
    }
}

/**
 * 饮品卡片
 */
@Composable
fun DrinkItemCard(
    drink: Drink,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageData = drink.imageUrl ?: "https://images.unsplash.com/photo-1558857563-b371033873b8?w=80&h=80&fit=crop"
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageData)
                    .crossfade(true)
                    .build(),
                contentDescription = drink.drinkName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 饮品信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = drink.drinkName ?: "未知饮品",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (drink.brand != null) {
                    Text(
                        text = "品牌: ${drink.brand}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                if (drink.category != null) {
                    Text(
                        text = "类别: ${drink.category}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    // 糖分
                    Text(
                        text = "糖分: ${String.format("%.1f", drink.sugarContent ?: 0f)}g",
                        fontSize = 13.sp,
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 热量
                    Text(
                        text = "热量: ${String.format("%.0f", drink.calories ?: 0f)}kcal",
                        fontSize = 13.sp,
                        color = Color(0xFFFF9500),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 健康评分
                val score = drink.healthScore ?: 50
                Text(
                    text = "健康评分: $score",
                    fontSize = 13.sp,
                    color = when {
                        score >= 80 -> Color(0xFF4CAF50)
                        score >= 60 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "查看详情",
                tint = Color.Gray
            )
        }
    }
}

/**
 * 添加饮品记录对话框
 */
@Composable
fun AddDrinkRecordDialog(
    drink: Drink,
    userId: Long,
    viewModel: DrinkViewModel,
    onDismiss: () -> Unit
) {
    val defaultVolume = drink.volume ?: 500f
    var portionSize by remember { mutableStateOf(defaultVolume.toString()) }
    var selectedMealType by remember { mutableStateOf("snack") }
    var notes by remember { mutableStateOf("") }
    
    val mealTypes = listOf(
        "breakfast" to "早餐",
        "lunch" to "午餐",
        "dinner" to "晚餐",
        "snack" to "加餐"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加饮品记录",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                item {
                    // 饮品名称
                    Text(
                        text = drink.drinkName ?: "未知饮品",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 营养信息
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Gray100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("营养信息 (每100ml)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("糖分: ${String.format("%.1f", drink.sugarContent ?: 0f)}g", fontSize = 13.sp)
                            Text("热量: ${String.format("%.0f", drink.calories ?: 0f)}kcal", fontSize = 13.sp)
                            Text("健康评分: ${drink.healthScore ?: 50}", fontSize = 13.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 份量输入
                    Text("份量 (ml)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = portionSize,
                        onValueChange = { portionSize = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("默认: ${defaultVolume}ml") },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 餐次选择
                    Text("餐次", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mealTypes.forEach { (type, label) ->
                            MyFilterChip(
                                selected = selectedMealType == type,
                                onClick = { selectedMealType = type },
                                label = label,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 备注
                    Text("备注", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("添加备注（可选）") },
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val portion = portionSize.toFloatOrNull() ?: defaultVolume
                    viewModel.addDrinkRecord(
                        userId = userId,
                        drinkId = drink.drinkId,
                        mealType = selectedMealType,
                        portionSize = portion,
                        notes = notes.ifEmpty { null }
                    )
                    onDismiss()
                }
            ) {
                Text("确认添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MintGreen,
            selectedLabelColor = Color.White
        )
    )
}

