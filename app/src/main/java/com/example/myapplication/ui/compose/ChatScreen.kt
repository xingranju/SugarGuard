package com.example.myapplication.ui.compose

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.db.DatabaseProvider
import com.example.myapplication.db.entity.ConversationEntity
import com.example.myapplication.model.ConversationHistory
import com.example.myapplication.model.UpdateFeedbackRequest
import com.example.myapplication.util.UserManager
import com.example.myapplication.util.DrinkImageUtil
import com.example.myapplication.viewmodel.AIServiceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import android.widget.Toast
import androidx.compose.ui.draw.clip

/**
 * 对话屏幕
 * 包含健康问答、食物推荐和历史记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AIServiceViewModel,
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ChatTab.QA) }
    
    val chatResponse by viewModel.chatResponse.observeAsState()
    val recommendationResult by viewModel.recommendationResult.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("AI 糖知助手") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = Color(0xFF333333)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF333333)
                    )
                )
                
                // 标签选择器
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    ChatTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 健康状态条
            HealthStatusBar()
            
            when (selectedTab) {
                ChatTab.QA -> HealthQAContent(
                    viewModel = viewModel,
                    chatResponse = chatResponse,
                    isLoading = isLoading
                )
                ChatTab.RECOMMENDATION -> DrinkRecommendationContent(
                    viewModel = viewModel,
                    recommendationResult = recommendationResult,
                    isLoading = isLoading
                )
                ChatTab.HISTORY -> ConversationHistoryContent()
            }
        }
    }
}

/**
 * 健康问答内容
 */
@Composable
fun HealthQAContent(
    viewModel: AIServiceViewModel,
    chatResponse: com.example.myapplication.model.ChatResponse?,
    isLoading: Boolean
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf<ChatMessage>()) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    val conversationDao = remember {
        try { DatabaseProvider.getDatabase().conversationDao() } catch (_: Exception) { null }
    }
    val sessionId = remember { "qa_${System.currentTimeMillis()}" }
    var historyLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!historyLoaded && conversationDao != null) {
            withContext(Dispatchers.IO) {
                try {
                    val allConvos = conversationDao.getAllConversationsSync(userId)
                    val messages = allConvos.map { entity ->
                        ChatMessage(content = entity.content, isUser = entity.role == "user")
                    }
                    if (messages.isNotEmpty()) {
                        chatHistory = messages
                    }
                } catch (_: Exception) {}
            }
            historyLoaded = true
            if (chatHistory.isNotEmpty()) {
                scrollState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    LaunchedEffect(chatResponse) {
        chatResponse?.let {
            if (it.isSuccess && it.response != null) {
                chatHistory = chatHistory + ChatMessage(content = it.response, isUser = false)
                conversationDao?.let { dao ->
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dao.insert(ConversationEntity(
                                userId = userId,
                                sessionId = sessionId,
                                role = "assistant",
                                content = it.response,
                                createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            ))
                        } catch (_: Exception) {}
                    }
                }
                coroutineScope.launch {
                    if (chatHistory.isNotEmpty()) {
                        scrollState.animateScrollToItem(chatHistory.size - 1)
                    }
                }
            }
        }
    }
    
    val suggestedQuestions = listOf(
        "🧃 推荐几款低糖饮品",
        "🍽️ 今天吃什么比较健康？",
        "📊 如何控制每日糖分摄入？",
        "🏃 运动后适合喝什么？",
        "🌙 晚餐吃什么不容易胖？",
        "🥗 有哪些低GI食物推荐？"
    )

    fun sendMessage(text: String) {
        val msgText = text.replace(Regex("^[\\p{So}\\p{Cn}]+ "), "")
        message = ""
        chatHistory = chatHistory + ChatMessage(content = msgText, isUser = true)
        conversationDao?.let { dao ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    dao.insert(ConversationEntity(
                        userId = userId,
                        sessionId = sessionId,
                        role = "user",
                        content = msgText,
                        createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    ))
                } catch (_: Exception) {}
            }
        }
        viewModel.chat(userId.toInt(), msgText)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatHistory.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MintBg,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 28.sp) }
                        }
                        Text("AI 糖知助手", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        Text(
                            "有任何健康问题都可以问我\n比如控糖建议、饮食推荐等",
                            fontSize = 14.sp, color = Gray500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
                item {
                    Text("试试问我：", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray600,
                        modifier = Modifier.padding(bottom = 8.dp))
                }
                item {
                    SuggestedQuestionsGrid(suggestedQuestions) { sendMessage(it) }
                }
            } else {
                items(chatHistory) { chatMessage ->
                    ChatBubble(message = chatMessage)
                }
                if (!isLoading && chatHistory.lastOrNull()?.isUser == false) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("继续提问：", fontSize = 12.sp, color = Gray400, modifier = Modifier.padding(bottom = 6.dp))
                        SuggestedQuestionsGrid(suggestedQuestions.take(3)) { sendMessage(it) }
                    }
                }
            }

            if (isLoading) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 200.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp), color = MintGreen, strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("AI 正在思考...", color = Gray500, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFFF0F0F0))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入您的健康问题...", fontSize = 14.sp) },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F7FA),
                    unfocusedContainerColor = Color(0xFFF5F7FA),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Surface(
                shape = CircleShape,
                color = if (message.isNotBlank() && !isLoading) MintGreen else Gray300,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(enabled = message.isNotBlank() && !isLoading) {
                        if (message.isNotBlank()) sendMessage(message)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Send, contentDescription = "发送", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestedQuestionsGrid(questions: List<String>, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        questions.forEach { question ->
            Surface(
                modifier = Modifier.clickable { onSelect(question) },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE8E8E8)),
                shadowElevation = 0.dp
            ) {
                Text(
                    question,
                    fontSize = 13.sp,
                    color = Gray700,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Surface(
                shape = CircleShape,
                color = MintBg,
                modifier = Modifier.size(32.dp).padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 14.sp) }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (message.isUser) {
            Surface(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
                color = MintGreen,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 14.sp, lineHeight = 20.sp, color = Color.White
                )
            }
        } else {
            Surface(
                modifier = Modifier.widthIn(max = 320.dp),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                FormattedAiResponse(message.content)
            }
        }
    }
}

@Composable
private fun FormattedAiResponse(content: String) {
    val lines = content.split("\n")
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.isBlank() -> Spacer(modifier = Modifier.height(4.dp))
                trimmed.startsWith("**") && trimmed.endsWith("**") && trimmed.length > 4 -> {
                    val headerText = trimmed.removePrefix("**").removeSuffix("**")
                    val hasEmoji = headerText.any { Character.getType(it).let { t -> t == Character.OTHER_SYMBOL.toInt() || t == Character.SURROGATE.toInt() } }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        headerText,
                        fontSize = if (hasEmoji) 14.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                }
                trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val numEnd = trimmed.indexOf('.')
                    val num = trimmed.substring(0, numEnd)
                    val rest = trimmed.substring(numEnd + 1).trim()
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = CircleShape,
                            color = MintGreen,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(num, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            buildFormattedText(rest),
                            fontSize = 14.sp, lineHeight = 20.sp, color = Gray700,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.startsWith("- ") -> {
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 1.dp, bottom = 1.dp), verticalAlignment = Alignment.Top) {
                        Text("•", fontSize = 14.sp, color = MintGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            buildFormattedText(trimmed.removePrefix("- ")),
                            fontSize = 13.sp, lineHeight = 19.sp, color = Gray600,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    Text(
                        buildFormattedText(trimmed),
                        fontSize = 14.sp, lineHeight = 21.sp, color = Gray700
                    )
                }
            }
        }
    }
}

private fun buildFormattedText(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldStart = remaining.indexOf("**")
            if (boldStart == -1) {
                append(remaining)
                break
            }
            append(remaining.substring(0, boldStart))
            val boldEnd = remaining.indexOf("**", boldStart + 2)
            if (boldEnd == -1) {
                append(remaining.substring(boldStart))
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))) {
                append(remaining.substring(boldStart + 2, boldEnd))
            }
            remaining = remaining.substring(boldEnd + 2)
        }
    }
}

/**
 * 饮品推荐内容 - 基于数据库饮品 + 用户画像 + AI分析
 */
@Composable
fun DrinkRecommendationContent(
    viewModel: AIServiceViewModel,
    recommendationResult: com.example.myapplication.model.RecommendationResponse?,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val recPrefs = context.getSharedPreferences("drink_rec_cache", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    data class RankedDrink(
        val drink: com.example.myapplication.model.Drink,
        val score: Int,
        var aiReason: String? = null,
        var aiTip: String? = null
    )

    var rankedDrinks by remember { mutableStateOf(listOf<RankedDrink>()) }
    var loadingState by remember { mutableStateOf("init") }
    var sugarLimit by remember { mutableFloatStateOf(25f) }

    fun loadRecommendations(forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            try {
                val cacheStr = recPrefs.getString("rec_full_${userId}", null)
                if (cacheStr != null) {
                    val gson = com.google.gson.Gson()
                    val arr = com.google.gson.JsonParser.parseString(cacheStr).asJsonArray
                    val cached = arr.mapNotNull { elem ->
                        try {
                            val obj = elem.asJsonObject
                            val drink = gson.fromJson(obj.get("drink"), com.example.myapplication.model.Drink::class.java)
                            RankedDrink(
                                drink = drink,
                                score = obj.get("score").asInt,
                                aiReason = obj.get("aiReason")?.asString?.takeIf { it.isNotEmpty() },
                                aiTip = obj.get("aiTip")?.asString?.takeIf { it.isNotEmpty() }
                            )
                        } catch (_: Exception) { null }
                    }
                    if (cached.isNotEmpty()) {
                        rankedDrinks = cached
                        loadingState = "loaded"
                        return
                    }
                }
            } catch (_: Exception) {}
        }

        loadingState = "loading"
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val drinksResp = RetrofitClient.getDrinkApiService().getAllDrinks().execute()
                    val drinks = if (drinksResp.isSuccessful && drinksResp.body()?.isSuccess == true)
                        drinksResp.body()?.data ?: emptyList() else emptyList()

                    if (drinks.isEmpty()) { loadingState = "empty"; return@withContext }

                    var userProfileStr = ""
                    val profileResp = RetrofitClient.getUserProfileApiService().getHealthProfile().execute()
                    if (profileResp.isSuccessful && profileResp.body()?.isSuccess == true) {
                        val p = profileResp.body()?.data
                        sugarLimit = p?.sugarLimit ?: 25f
                        if (p != null) {
                            val genderStr = when (p.gender) { "male" -> "男"; "female" -> "女"; else -> p.gender }
                            val actStr = when (p.activityLevel) { "sedentary" -> "久坐"; "light" -> "轻度活动"; "moderate" -> "中度活动"; "active" -> "活跃"; "very_active" -> "非常活跃"; else -> p.activityLevel ?: "未知" }
                            userProfileStr = "${p.age}岁${genderStr}，身高${p.height?.toInt()}cm，体重${p.weight?.toInt()}kg，${actStr}" +
                                    (if (!p.healthConditions.isNullOrBlank()) "，健康状况：${p.healthConditions}" else "") +
                                    (if (!p.allergies.isNullOrBlank()) "，过敏：${p.allergies}" else "")
                        }
                    }

                    val prefMap = mutableMapOf<Int, Int>()
                    val prefCategorySet = mutableSetOf<String>()
                    var prefSummary = ""
                    try {
                        val prefResp = RetrofitClient.getDrinkPreferenceApiService().getUserPreferences().execute()
                        if (prefResp.isSuccessful && prefResp.body()?.isSuccess == true) {
                            val userPrefs = prefResp.body()?.data ?: emptyList()
                            userPrefs.forEach { pref ->
                                if (pref.drinkId > 0) prefMap[pref.drinkId] = pref.preferenceScore ?: 3
                                pref.category?.let { prefCategorySet.add(it) }
                            }
                            if (userPrefs.isNotEmpty()) {
                                val topPrefs = userPrefs.sortedByDescending { it.preferenceScore ?: 0 }.take(5)
                                prefSummary = topPrefs.joinToString("、") { "${it.drinkName ?: "未知"}(偏好${it.preferenceScore}/5)" }
                            }
                        }
                    } catch (_: Exception) {}

                    var recentHealthStr = ""
                    var avgRecentSugar = 0.0
                    try {
                        val healthResp = RetrofitClient.getDailyHealthRecordApiService().getRecentRecords(3).execute()
                        if (healthResp.isSuccessful && healthResp.body()?.isSuccess == true) {
                            val records = healthResp.body()?.data
                            if (!records.isNullOrEmpty()) {
                                avgRecentSugar = records.mapNotNull { it.totalSugarIntake }.average()
                                val avgCal = records.mapNotNull { it.totalCalories }.average()
                                recentHealthStr = "近3日平均糖分${avgRecentSugar.toInt()}g，热量${avgCal.toInt()}kcal"
                            }
                        }
                    } catch (_: Exception) {}

                    val scored = drinks.map { drink ->
                        val sugar = drink.sugarContent ?: 50f
                        val healthBase = ((drink.healthScore ?: 50) * 40) / 100
                        val sugarBonus = when { sugar <= 5f -> 15; sugar <= 15f -> 8; sugar <= 25f -> 0; else -> -10 }
                        val calBonus = if ((drink.calories ?: 200f) < 100f) 5 else 0
                        val prefBonus = prefMap[drink.drinkId]?.let { ps -> ((ps - 1) * 30) / 4 } ?: 0
                        val catBonus = if (drink.category != null && prefCategorySet.contains(drink.category)) 10 else 0
                        val recentHealthBonus = if (avgRecentSugar > sugarLimit * 0.8 && sugar <= 10f) 5 else 0
                        val randomBonus = kotlin.random.Random.nextInt(-5, 6)
                        RankedDrink(drink, (healthBase + sugarBonus + calBonus + prefBonus + catBonus + recentHealthBonus + randomBonus).coerceIn(0, 100))
                    }.sortedByDescending { it.score }.take(30)

                    val aiApi = RetrofitClient.getAIApiService()
                    val top6 = scored.take(6)
                    val topInfoStr = top6.joinToString("\n") { r ->
                        val isPref = prefMap.containsKey(r.drink.drinkId)
                        "${r.drink.drinkName}（${r.drink.brand ?: ""}，糖${r.drink.sugarContent ?: 0}g，热量${r.drink.calories?.toInt() ?: 0}kcal，健康分${r.score}${if (isPref) "，用户偏好饮品" else ""}）"
                    }

                    val prompt = buildString {
                        append("你是一位温柔亲切、专业的控糖营养顾问。")
                        if (userProfileStr.isNotBlank()) append("用户信息：${userProfileStr}。")
                        append("用户每日糖分目标${sugarLimit.toInt()}g。")
                        if (recentHealthStr.isNotBlank()) append("${recentHealthStr}。")
                        if (prefSummary.isNotBlank()) append("用户饮品偏好：${prefSummary}。")
                        append("以下是为用户精选的6款饮品：\n${topInfoStr}\n\n")
                        append("请为每款饮品写推荐内容，要求：\n")
                        append("1. 推荐理由2-3句话，务必结合用户档案（年龄、身高体重、健康状况、活动量等）和具体饮品的营养数据做详细分析\n")
                        append("2. 如果用户有饮品偏好，要明确提及用户偏好并适当推荐（如'考虑到您偏好xx类饮品'）\n")
                        append("3. 结合用户近期健康记录分析（如近日糖分摄入偏高则推荐低糖，偏低则可适当放松）\n")
                        append("4. 平衡健康考虑：如果偏好饮品含糖较高要温和提醒，同时给出替代建议\n")
                        append("5. 一句实用小贴士（如搭配建议、最佳饮用时间、替代方案等）\n\n")
                        append("格式严格如下（每款一行，用|分隔，理由和贴士之间用|分隔）：\n")
                        append("饮品名|推荐理由（2-3句话）|小贴士\n\n")
                        append("不要加序号、markdown格式，只要6行纯文本。")
                    }

                    val finalList = scored.toMutableList()
                    try {
                        val aiResp = aiApi.chat(com.example.myapplication.model.ChatRequest(userId.toInt(), prompt, false)).execute()
                        if (aiResp.isSuccessful && aiResp.body()?.isSuccess == true) {
                            val aiText = aiResp.body()?.data?.response ?: ""
                            val lines = aiText.lines().filter { it.contains("|") }
                            lines.forEach { line ->
                                val parts = line.split("|").map { it.trim().replace("**", "").replace("*", "") }
                                if (parts.size >= 3) {
                                    val name = parts[0].replace(Regex("^\\d+[.、)）]\\s*"), "")
                                    val idx = finalList.indexOfFirst { it.drink.drinkName?.contains(name) == true || name.contains(it.drink.drinkName ?: "___") }
                                    if (idx >= 0) {
                                        finalList[idx] = finalList[idx].copy(aiReason = parts[1], aiTip = parts[2])
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}

                    rankedDrinks = finalList
                    loadingState = "loaded"

                    try {
                        val cacheArr = com.google.gson.JsonArray()
                        finalList.forEach { r ->
                            val obj = com.google.gson.JsonObject()
                            obj.add("drink", com.google.gson.Gson().toJsonTree(r.drink))
                            obj.addProperty("score", r.score)
                            obj.addProperty("aiReason", r.aiReason ?: "")
                            obj.addProperty("aiTip", r.aiTip ?: "")
                            cacheArr.add(obj)
                        }
                        recPrefs.edit().putString("rec_full_${userId}", cacheArr.toString()).apply()
                    } catch (_: Exception) {}

                } catch (_: Exception) { loadingState = "error" }
            }
        }
    }

    LaunchedEffect(Unit) { loadRecommendations(forceRefresh = false) }

    val mealViewModel: com.example.myapplication.viewmodel.LocalMealViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uid = userId.toInt()

    var showRecAddSheet by remember { mutableStateOf(false) }
    var pendingRecDrinkName by remember { mutableStateOf("") }
    var pendingRecSugar by remember { mutableDoubleStateOf(0.0) }
    var pendingRecCalories by remember { mutableDoubleStateOf(0.0) }
    var pendingRecImageUrl by remember { mutableStateOf<String?>(null) }
    var pendingRecNotes by remember { mutableStateOf("") }
    var pendingRecVolume by remember { mutableDoubleStateOf(500.0) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { loadRecommendations(forceRefresh = true) },
            enabled = loadingState != "loading",
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(4.dp)
        ) {
            Icon(Icons.Default.Refresh, "刷新", modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("刷新推荐", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        when (loadingState) {
            "loading", "init" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MintGreen)
                    Spacer(Modifier.height(8.dp))
                    Text("正在分析您的健康数据...", fontSize = 13.sp, color = Gray500)
                }
            }
            "error", "empty" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无推荐数据，请稍后重试", fontSize = 14.sp, color = Gray400)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("为您推荐 ${rankedDrinks.size} 款饮品", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800)
                        Text("基于您的健康档案和控糖目标智能排序", fontSize = 12.sp, color = Gray400)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(rankedDrinks) { ranked ->
                        val drink = ranked.drink
                        val isTop = rankedDrinks.indexOf(ranked) < 6
                        val rank = rankedDrinks.indexOf(ranked) + 1
                        var aiExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(if (isTop) 4.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    if (!drink.imageUrl.isNullOrEmpty()) {
                                        coil.compose.AsyncImage(
                                            model = drink.imageUrl,
                                            contentDescription = drink.drinkName,
                                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(12.dp))
                                    } else if (isTop) {
                                        val rankColors = listOf(Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFCD7F32), MintGreen, Color(0xFF7986CB), Color(0xFF90A4AE))
                                        Surface(shape = CircleShape, color = rankColors.getOrElse(rank - 1) { Gray400 }, modifier = Modifier.size(40.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("$rank", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(drink.drinkName ?: "未知", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray800)
                                        if (drink.brand != null) Text("${drink.brand}${if ((drink.volume ?: 0f) > 0f) " · ${(drink.volume ?: 0f).toInt()}ml" else ""}", fontSize = 12.sp, color = Gray400)
                                    }
                                    Surface(shape = RoundedCornerShape(12.dp), color = when {
                                        ranked.score >= 80 -> Color(0xFFE8F5E9); ranked.score >= 60 -> MintBg
                                        ranked.score >= 40 -> Color(0xFFFFF3E0); else -> Color(0xFFFFEBEE)
                                    }) {
                                        Text(
                                            "${ranked.score}分", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                            color = when { ranked.score >= 80 -> Color(0xFF2E7D32); ranked.score >= 60 -> MintGreen; ranked.score >= 40 -> Color(0xFFEF6C00); else -> Color(0xFFE53935) },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Cookie, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(14.dp))
                                        Text("${String.format("%.1f", drink.sugarContent ?: 0f)}g 糖", fontSize = 12.sp, color = Color(0xFFFF6B6B))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                                        Text("${(drink.calories ?: 0f).toInt()}kcal", fontSize = 12.sp, color = Color(0xFFFF9800))
                                    }
                                }

                                if (isTop) {
                                    Spacer(Modifier.height(10.dp))
                                    val reasonText = ranked.aiReason ?: run {
                                        val sugar = drink.sugarContent ?: 0f
                                        when {
                                            sugar <= 5f -> "低糖健康之选，含糖量极低，适合日常饮用。综合健康评分${ranked.score}分，推荐放心品尝。"
                                            sugar <= 15f -> "含糖量适中，健康评分${ranked.score}分。适量饮用不会给身体带来太大负担。"
                                            sugar <= 25f -> "口感不错但含糖偏高，建议搭配低糖餐食。可以选择少糖版本，更健康哦。"
                                            else -> "含糖量较高，偶尔解馋可以，但建议控制频率。试试减糖或选择替代品。"
                                        }
                                    }
                                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MintBg)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(Icons.Default.Psychology, null, tint = MintGreen, modifier = Modifier.size(16.dp))
                                                    Text("AI推荐理由", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                                                }
                                                IconButton(onClick = { aiExpanded = !aiExpanded }, modifier = Modifier.size(24.dp)) {
                                                    Icon(
                                                        if (aiExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        "展开", tint = MintGreen, modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            val displayText = if (aiExpanded) reasonText else reasonText.take(40).let { if (it.length < reasonText.length) "$it..." else it }
                                            Spacer(Modifier.height(4.dp))
                                            Text(displayText, fontSize = 13.sp, color = Gray700, lineHeight = 20.sp)
                                            if (aiExpanded && ranked.aiTip != null) {
                                                Spacer(Modifier.height(8.dp))
                                                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.6f)) {
                                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("💡", fontSize = 14.sp)
                                                        Text(ranked.aiTip!!, fontSize = 12.sp, color = Gray600, lineHeight = 18.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.height(8.dp))
                                    Text("适量饮用，注意搭配低糖餐食，保持每日总糖分在目标范围内哦～", fontSize = 11.sp, color = Gray400, lineHeight = 16.sp)
                                }

                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val noteText = buildString {
                                            append("AI推荐(${ranked.score}分)")
                                            if (ranked.aiReason != null) {
                                                append(" | ${ranked.aiReason}")
                                            } else {
                                                val sugar = drink.sugarContent ?: 0f
                                                val desc = when {
                                                    sugar <= 5f -> "低糖健康之选，放心饮用"
                                                    sugar <= 15f -> "含糖适中，适量饮用为佳"
                                                    sugar <= 25f -> "含糖偏高，建议少量品尝"
                                                    else -> "高糖饮品，偶尔解馋即可"
                                                }
                                                append(" | $desc")
                                            }
                                            if (ranked.aiTip != null) {
                                                append(" | 小贴士：${ranked.aiTip}")
                                            }
                                        }
                                        pendingRecDrinkName = drink.drinkName ?: "饮品"
                                        pendingRecSugar = (drink.sugarContent ?: 0f).toDouble()
                                        pendingRecCalories = (drink.calories ?: 0f).toDouble()
                                        pendingRecImageUrl = drink.imageUrl
                                        pendingRecNotes = noteText
                                        pendingRecVolume = (drink.volume ?: 500f).toDouble()
                                        showRecAddSheet = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp), shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = Color.White),
                                    elevation = ButtonDefaults.buttonElevation(0.dp), contentPadding = PaddingValues(0.dp)
                                ) { Text("加入日记", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showRecAddSheet) {
        MealDrinkAddBottomSheet(
            drinkName = pendingRecDrinkName,
            sugar = pendingRecSugar,
            calories = pendingRecCalories,
            imageUrl = pendingRecImageUrl,
            defaultNotes = pendingRecNotes,
            requireExplicitMeal = true,
            servingSize = pendingRecVolume,
            servingSizeUnit = "ml",
            onDismiss = { showRecAddSheet = false },
            onSave = { mealType, portionSize, notes, multiplier ->
                scope.launch {
                    mealViewModel.addMeal(
                        userId = uid,
                        foodName = pendingRecDrinkName,
                        sugarContent = pendingRecSugar * multiplier,
                        calories = pendingRecCalories * multiplier,
                        protein = null,
                        fat = null,
                        carbohydrate = null,
                        portionSize = portionSize,
                        notes = notes,
                        mealType = mealType,
                        imageUrl = pendingRecImageUrl
                    )
                    Toast.makeText(context, "已添加「$pendingRecDrinkName」到日记", Toast.LENGTH_SHORT).show()
                }
                showRecAddSheet = false
            }
        )
    }
    }
}

/**
 * 饮品推荐卡片
 */
@Composable
fun DrinkRecommendationCard(
    drink: com.example.myapplication.model.RecommendationResponse.DrinkRecommendation,
    onAddToDiary: ((String, Double, Double) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 饮品图片 - 优先使用本地图片
            val localImageUri = DrinkImageUtil.getImageUri(drink.drinkName)
            val imageData = localImageUri ?: drink.imageUrl ?: ""
            
            AsyncImage(
                model = imageData,
                contentDescription = drink.drinkName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            // 饮品信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 名称和健康评分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = drink.drinkName ?: "未知饮品",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${drink.healthScore}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                drink.healthScore >= 70 -> Color(0xFF4CAF50)
                                drink.healthScore >= 40 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }
                }
                
                // 品牌和容量
                if (drink.brand != null || drink.volume > 0) {
                    Text(
                        text = buildString {
                            drink.brand?.let { append(it) }
                            if (drink.brand != null && drink.volume > 0) append(" · ")
                            if (drink.volume > 0) append("${drink.volume.toInt()}ml")
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // 营养信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 糖分
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Cookie,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", drink.sugarContent)}g 糖",
                            fontSize = 12.sp,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                    
                    // 热量
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${String.format("%.0f", drink.calories)}kcal",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
                
                // 推荐理由
                if (drink.reason != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = drink.reason,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // AI健康建议（仅前8个推荐有）
                if (drink.aiHealthAdvice != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0F7FF)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "AI健康建议",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                                Text(
                                    text = drink.aiHealthAdvice,
                                    fontSize = 11.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                onAddToDiary?.let { addToDiary ->
                    val context = LocalContext.current
                    var added by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            addToDiary(
                                drink.drinkName ?: "未知饮品",
                                drink.sugarContent.toDouble(),
                                drink.calories.toDouble()
                            )
                            added = true
                            android.widget.Toast.makeText(context, "已加入今日日记", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (added) Color(0xFFF0F0F0) else MintGreen,
                            contentColor = if (added) Color(0xFFBDBDBD) else Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        enabled = !added,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            if (added) "✓ 已加入" else "加入日记",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val content: String,
    val isUser: Boolean
)

/**
 * 对话标签枚举
 */
enum class ChatTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    QA("健康问答", Icons.Default.QuestionAnswer),
    RECOMMENDATION("饮品推荐", Icons.Default.Recommend),
    HISTORY("历史记录", Icons.Default.History)
}



/**
 * 对话历史内容显示
 */
@Composable
fun ConversationHistoryContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = UserManager.getInstance(context).currentUserId.toLong()

    var conversations by remember { mutableStateOf<List<ConversationHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.getConversationHistoryApiService()
                    .getUserConversations(limit = 50)
                    .awaitResponse()
                
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    conversations = response.body()?.data ?: emptyList()
                } else {
                    val msg = response.body()?.message ?: "未知错误"
                    errorMessage = "加载失败: $msg"
                }
            } catch (e: Exception) {
                errorMessage = "加载失败: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            conversations.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF1F8E9)
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Text(
                                text = "暂无对话历史",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "开始使用健康问答功能\n与AI助手对话吧！",
                                fontSize = 14.sp,
                                color = Color(0xFF558B2F),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversations) { conversation ->
                        ConversationCard(
                            conversation = conversation,
                            onDelete = { id ->
                                scope.launch {
                                    try {
                                        val deleteResponse = RetrofitClient.getConversationHistoryApiService()
                                            .deleteConversation(id)
                                            .awaitResponse()
                                        
                                        if (deleteResponse.isSuccessful) {
                                            conversations = conversations.filter { it.conversationId != id }
                                        }
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            },
                            onFeedback = { id, feedback ->
                                scope.launch {
                                    try {
                                        val resp = RetrofitClient.getConversationHistoryApiService()
                                            .updateFeedback(id, UpdateFeedbackRequest(feedback))
                                            .awaitResponse()
                                        if (resp.isSuccessful) {
                                            conversations = conversations.map {
                                                if (it.conversationId == id) it.copy(feedback = feedback) else it
                                            }
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 对话卡片
 */
@Composable
fun ConversationCard(
    conversation: ConversationHistory,
    onDelete: (Int) -> Unit,
    onFeedback: (Int, Int) -> Unit
) {
    var showFullResponse by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: timestamp + expand/delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = conversation.createdAt,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "展开/折叠",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDelete(conversation.conversationId) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User message - 始终显示
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = conversation.message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f),
                            lineHeight = 20.sp
                        )
                    }
                    
                    // 如果未展开，显示提示
                    if (!expanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击展开查看AI回答",
                            fontSize = 11.sp,
                            color = Color(0xFF81C784),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            // AI response - 只在展开时显示
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                
                val displayText = if (showFullResponse || conversation.response.length <= 100) {
                    conversation.response
                } else {
                    conversation.response.take(100) + "..."
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = displayText,
                            fontSize = 14.sp,
                            color = Color(0xFF424242),
                            modifier = Modifier.weight(1f),
                            lineHeight = 20.sp
                        )
                    }
                }

                if (conversation.response.length > 100) {
                    TextButton(onClick = { showFullResponse = !showFullResponse }) {
                        Text(
                            text = if (showFullResponse) "收起" else "展开全部",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Feedback stars
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("评分:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    (1..5).forEach { index ->
                        IconButton(
                            onClick = { onFeedback(conversation.conversationId, index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if ((conversation.feedback ?: 0) >= index) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "评分$index",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 健康状态条
 */
@Composable
fun HealthStatusBar() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var todaySugar by remember { mutableStateOf(0f) }
    var todayCalories by remember { mutableStateOf(0f) }
    var healthScore by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                val userId = prefs.getLong("user_id", 1)
                val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val mealResp = RetrofitClient.getMealApiService()
                        .getDailyMeals(userId.toInt(), today).execute()
                    if (mealResp.isSuccessful && mealResp.body()?.isSuccess == true) {
                        val data = mealResp.body()!!.data
                        val meals = data?.get("meals")
                        if (meals is List<*>) {
                            var sugar = 0f
                            var cals = 0f
                            for (m in meals) {
                                if (m is Map<*, *>) {
                                    sugar += ((m["sugar_content"] ?: m["sugarContent"]) as? Number)?.toFloat() ?: 0f
                                    cals += ((m["calories"]) as? Number)?.toFloat() ?: 0f
                                }
                            }
                            todaySugar = sugar
                            todayCalories = cals
                        } else {
                            todaySugar = (data?.get("total_sugar") as? Number)?.toFloat() ?: 0f
                            todayCalories = (data?.get("total_calories") as? Number)?.toFloat() ?: 0f
                        }
                    }

                    val profileResp = RetrofitClient.getUserProfileApiService()
                        .getHealthProfile().execute()
                    val sugarLimit = if (profileResp.isSuccessful && profileResp.body()?.isSuccess == true) {
                        profileResp.body()!!.data?.sugarLimit ?: 25f
                    } else 25f

                    healthScore = (100 - (todaySugar / sugarLimit * 30).toInt()).coerceIn(0, 100)
                }
            } catch (_: Exception) {
                healthScore = 0
            } finally {
                isLoading = false
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 健康评分
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "健康评分",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "$healthScore",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        healthScore >= 80 -> Color(0xFF4CAF50)
                        healthScore >= 60 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }
            
            // 今日糖分
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Cookie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "糖分",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${String.format("%.1f", todaySugar)}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 今日热量
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "热量",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${String.format("%.0f", todayCalories)}kcal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * 计算健康评分
 */
fun calculateHealthScore(sugar: Float, calories: Float): Int {
    // 推荐每日糖分摄入量：25-50g
    // 推荐每日热量：1800-2400kcal (成年人平均)
    val sugarScore = when {
        sugar <= 25 -> 100
        sugar <= 50 -> 80
        sugar <= 75 -> 60
        sugar <= 100 -> 40
        else -> 20
    }
    
    val calorieScore = when {
        calories in 1800f..2400f -> 100
        calories in 1500f..1800f || calories in 2400f..2700f -> 80
        calories in 1200f..1500f || calories in 2700f..3000f -> 60
        else -> 40
    }
    
    return ((sugarScore + calorieScore) / 2.0).toInt()
}
