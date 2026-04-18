package com.example.myapplication.ui.compose

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.viewmodel.AIServiceViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * AI服务主界面 (Compose)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIServiceScreen(
    viewModel: AIServiceViewModel,
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // 观察ViewModel状态
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val drinkRecognitionResult by viewModel.drinkRecognitionResult.observeAsState()
    val chatResponse by viewModel.chatResponse.observeAsState()
    val recommendationResult by viewModel.recommendationResult.observeAsState()
    val healthAnalysisResult by viewModel.healthAnalysisResult.observeAsState()
    
    // 界面状态
    var chatMessage by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf("AI助手将在这里回复您\n\n") }
    var resultText by remember { mutableStateOf("欢迎使用SugarGuard AI助手!\n\n功能介绍:\n· 拍照识别食物并分析糖分\n· 向AI助手提问健康问题\n· 获取个性化食物推荐\n· 查看健康数据分析\n\n请选择功能开始使用") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // 辅助函数：将URI转换为文件
    fun uriToFile(uri: Uri, context: Context): File? {
        return try {
            Log.d("AIServiceScreen", "开始转换URI: $uri")
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("AIServiceScreen", "无法打开输入流")
                return null
            }
            
            val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            Log.d("AIServiceScreen", "目标文件: ${tempFile.absolutePath}")
            
            val outputStream = FileOutputStream(tempFile)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    val bytesCopied = input.copyTo(output)
                    Log.d("AIServiceScreen", "复制了 $bytesCopied 字节")
                }
            }
            
            if (tempFile.exists() && tempFile.length() > 0) {
                Log.d("AIServiceScreen", "图片转换成功: ${tempFile.absolutePath}, 大小: ${tempFile.length()} bytes")
                tempFile
            } else {
                Log.e("AIServiceScreen", "文件创建失败或为空")
                null
            }
        } catch (e: SecurityException) {
            Log.e("AIServiceScreen", "权限异常", e)
            android.widget.Toast.makeText(context, "缺少存储权限", android.widget.Toast.LENGTH_LONG).show()
            null
        } catch (e: Exception) {
            Log.e("AIServiceScreen", "图片转换失败: ${e.message}", e)
            e.printStackTrace()
            android.widget.Toast.makeText(context, "文件转换失败: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            null
        }
    }
    
    // 相机拍照启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedImageBitmap = it
            resultText = "⏳ 正在识别饮品...\n请稍候..."
            
            try {
                // 将Bitmap保存为临时文件
                val tempFile = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(tempFile)
                it.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()
                
                Log.d("AIServiceScreen", "相机图片已保存: ${tempFile.absolutePath}")
                
                // 调用识别API
                viewModel.recognizeDrink(userId, tempFile)
            } catch (e: Exception) {
                Log.e("AIServiceScreen", "处理相机图片失败", e)
                resultText = "❌ 图片处理失败: ${e.message}"
            }
        }
    }
    
    // 图库图片选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        try {
            Log.d("AIServiceScreen", "=== 图库选择器回调开始 ===")
            Log.d("AIServiceScreen", "URI: $uri")
            
            if (uri == null) {
                Log.w("AIServiceScreen", "用户取消了图片选择")
                android.widget.Toast.makeText(context, "未选择图片", android.widget.Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            
            Log.d("AIServiceScreen", "URI有效,开始加载图片")
            android.widget.Toast.makeText(context, "正在加载图片...", android.widget.Toast.LENGTH_SHORT).show()
            
            // 加载图片预览
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    Log.d("AIServiceScreen", "使用ImageDecoder加载")
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    Log.d("AIServiceScreen", "使用MediaStore加载")
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                
                selectedImageBitmap = bitmap
                Log.d("AIServiceScreen", "✅ 图片加载成功! 尺寸: ${bitmap.width}x${bitmap.height}")
                
            } catch (e: Exception) {
                Log.e("AIServiceScreen", "❌ 图片解码失败", e)
                resultText = "❌ 图片解码失败: ${e.message}"
                android.widget.Toast.makeText(context, "图片解码失败", android.widget.Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }
            
            resultText = "⏳ 正在识别饮品...\n请稍候..."
            android.widget.Toast.makeText(context, "正在识别饮品...", android.widget.Toast.LENGTH_SHORT).show()
            
            // 将URI转换为文件
            Log.d("AIServiceScreen", "开始URI转文件")
            val imageFile = uriToFile(uri, context)
            
            if (imageFile != null && imageFile.exists()) {
                Log.d("AIServiceScreen", "✅ 文件转换成功!")
                Log.d("AIServiceScreen", "准备调用识别API:")
                Log.d("AIServiceScreen", "  - 用户ID: $userId")
                Log.d("AIServiceScreen", "  - 文件路径: ${imageFile.absolutePath}")
                Log.d("AIServiceScreen", "  - 文件大小: ${imageFile.length()} bytes")
                
                // 调用识别API
                try {
                    viewModel.recognizeDrink(userId, imageFile)
                    Log.d("AIServiceScreen", "✅ recognizeDrink方法已调用")
                } catch (e: Exception) {
                    Log.e("AIServiceScreen", "❌ 调用识别API失败", e)
                    resultText = "❌ API调用失败: ${e.message}"
                    android.widget.Toast.makeText(context, "API调用失败", android.widget.Toast.LENGTH_LONG).show()
                }
            } else {
                val errorMsg = "❌ 图片文件读取失败\n请重试"
                resultText = errorMsg
                Log.e("AIServiceScreen", "❌ 文件转换失败: imageFile = $imageFile")
                android.widget.Toast.makeText(context, "文件读取失败", android.widget.Toast.LENGTH_SHORT).show()
            }
            
            Log.d("AIServiceScreen", "=== 图库选择器回调结束 ===")
            
        } catch (e: SecurityException) {
            Log.e("AIServiceScreen", "❌ 权限异常", e)
            e.printStackTrace()
            resultText = "❌ 权限错误: ${e.message}"
            android.widget.Toast.makeText(context, "缺少必要权限", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: OutOfMemoryError) {
            Log.e("AIServiceScreen", "❌ 内存不足", e)
            e.printStackTrace()
            resultText = "❌ 内存不足"
            android.widget.Toast.makeText(context, "图片过大,内存不足", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            val errorMsg = "❌ 未知错误: ${e.javaClass.simpleName} - ${e.message}"
            resultText = errorMsg
            Log.e("AIServiceScreen", "❌ 未知异常", e)
            e.printStackTrace()
            android.widget.Toast.makeText(context, "发生错误: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    // 监听响应结果 - 使用derivedStateOf避免无限重组
    val displayDrinkResult by remember {
        derivedStateOf {
            drinkRecognitionResult?.let { result ->
                if (result.isSuccess) {
                    // 检查数据完整性,避免NullPointerException
                    if (result.recognition != null && 
                        result.nutrition != null && 
                        result.healthAssessment != null) {
                        buildString {
                            append("🥤 识别结果\n\n")
                            append("饮品: ${result.recognition.drinkName ?: "未知"}\n")
                            append("置信度: ${(result.recognition.confidence * 100).toInt()}%\n\n")
                            append("营养信息\n")
                            append("含糖量: ${result.nutrition.sugarContent ?: 0}g\n")
                            append("热量: ${result.nutrition.calories ?: 0}kcal\n")
                            append("健康评分: ${result.nutrition.healthScore ?: 0}/100\n\n")
                            append("💡 健康建议\n")
                            append(result.healthAssessment.healthAdvice ?: "暂无建议")
                        }
                    } else {
                        "❌ 识别失败\n\n数据不完整,请重新尝试"
                    }
                } else {
                    "❌ 识别失败\n\n请重新尝试"
                }
            }
        }
    }
    
    // 当有新结果时更新显示
    LaunchedEffect(Unit) {
        snapshotFlow { drinkRecognitionResult }
            .collect { result ->
                Log.d("AIServiceScreen", "📊 收到drinkRecognitionResult更新: $result")
                result?.let {
                    Log.d("AIServiceScreen", "  - success: ${it.isSuccess}")
                    Log.d("AIServiceScreen", "  - recognition: ${it.recognition}")
                    Log.d("AIServiceScreen", "  - nutrition: ${it.nutrition}")
                    Log.d("AIServiceScreen", "  - healthAssessment: ${it.healthAssessment}")
                    
                    if (it.isSuccess) {
                        // 检查数据完整性,避免NullPointerException
                        if (it.recognition != null && 
                            it.nutrition != null && 
                            it.healthAssessment != null) {
                            Log.d("AIServiceScreen", "✅ 数据完整,开始构建结果文本")
                            resultText = buildString {
                                append("🥤 识别结果\n\n")
                                append("饮品: ${it.recognition.drinkName ?: "未知"}\n")
                                append("置信度: ${(it.recognition.confidence * 100).toInt()}%\n\n")
                                append("营养信息\n")
                                append("含糖量: ${it.nutrition.sugarContent ?: 0}g\n")
                                append("热量: ${it.nutrition.calories ?: 0}kcal\n")
                                append("健康评分: ${it.nutrition.healthScore ?: 0}/100\n\n")
                                append("💡 健康建议\n")
                                append(it.healthAssessment.healthAdvice ?: "暂无建议")
                            }
                            Log.d("AIServiceScreen", "✅ 结果文本已更新: ${resultText.take(50)}...")
                            
                            // 显示Toast提示用户结果已更新
                            android.widget.Toast.makeText(
                                context, 
                                "✅ 识别成功: ${it.recognition.drinkName ?: "未知"}", 
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Log.w("AIServiceScreen", "❌ 数据不完整!")
                            resultText = "❌ 识别失败\n\n数据不完整,请重新尝试"
                        }
                    } else {
                        Log.w("AIServiceScreen", "❌ success=false")
                        resultText = "❌ 识别失败\n\n请重新尝试"
                    }
                }
            }
    }
    
    LaunchedEffect(Unit) {
        snapshotFlow { chatResponse }
            .collect { response ->
                response?.let {
                    if (it.isSuccess) {
                        chatHistory += "🤖 AI助手: ${it.response}\n\n"
                    }
                }
            }
    }
    
    LaunchedEffect(Unit) {
        snapshotFlow { recommendationResult }
            .collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        resultText = buildString {
                            append("🌟 为您推荐 ${it.recommendationCount} 款饮品\n\n")
                            it.recommendations.forEachIndexed { index, drink ->
                                append("${index + 1}. 推荐饮品\n")
                            }
                        }
                    }
                }
            }
    }
    
    LaunchedEffect(Unit) {
        snapshotFlow { healthAnalysisResult }
            .collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        resultText = buildString {
                            append("健康分析报告\n\n")
                            
                            it.userProfile?.let { profile ->
                                append("基本信息\n")
                                profile["age"]?.let { append("年龄: ${it}岁\n") }
                                profile["height"]?.let { append("身高: ${it}cm\n") }
                                profile["weight"]?.let { append("体重: ${it}kg\n") }
                                append("\n")
                            }
                            
                            it.bmiAnalysis?.let { bmi ->
                                append("BMI分析\n")
                                append("BMI: ${bmi.bmi}\n")
                                append("分类: ${bmi.category}\n")
                                append("状态: ${bmi.healthStatus}\n")
                                val bmiAdvice = bmi.aiAdvice?.takeIf { it.isNotBlank() } ?: bmi.advice
                                bmiAdvice?.let { append("AI建议: $it\n") }
                                append("\n")
                            }
                            
                            it.sugarAssessment?.let { sugar ->
                                append("糖分摄入评估\n")
                                append("平均摄入: ${sugar.averageIntake}g/天\n")
                                append("建议限制: ${sugar.limit}g/天\n")
                                append("评估: ${sugar.assessment}\n")
                                val sugarAdvice = sugar.aiAdvice?.takeIf { it.isNotBlank() } ?: sugar.advice
                                sugarAdvice?.let { append("AI建议: $it") }
                            }
                        }
                    }
                }
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SugarGuard AI 助手") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 加载指示器
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 错误提示 - 使用安全调用防止null
            if (errorMessage?.isNotEmpty() == true) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            fontSize = 14.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // 1. 食物识别卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "食物识别",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                            Text(
                                text = "拍照识别并分析糖分含量",
                                fontSize = 13.sp,
                                color = Gray600
                            )
                        }
                    }
                    
                    // 图片预览
                    selectedImageBitmap?.let { bitmap ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "选中的图片",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Log.d("AIServiceScreen", "拍照按钮被点击")
                                android.widget.Toast.makeText(context, "打开相机拍照", android.widget.Toast.LENGTH_SHORT).show()
                                cameraLauncher.launch(null)
                                Log.d("AIServiceScreen", "相机已启动")
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.PhotoCamera, "拍照")
                            Spacer(Modifier.width(4.dp))
                            Text("拍照")
                        }
                        Button(
                            onClick = { 
                                Log.d("AIServiceScreen", "相册按钮被点击")
                                android.widget.Toast.makeText(context, "打开相册选择图片", android.widget.Toast.LENGTH_SHORT).show()
                                galleryLauncher.launch("image/*")
                                Log.d("AIServiceScreen", "图库选择器已启动")
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Image, "相册")
                            Spacer(Modifier.width(4.dp))
                            Text("相册")
                        }
                    }
                    
                    // 提示信息
                    if (!isLoading && selectedImageBitmap == null) {
                        Text(
                            text = "💡 支持JPG、PNG等常见图片格式",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // 2. 智能对话卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💬 健康问答",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "向AI助手提问健康相关问题",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    // 聊天历史
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Text(
                            text = chatHistory,
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState()),
                            fontSize = 14.sp
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessage,
                            onValueChange = { chatMessage = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("输入健康问题...") },
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (chatMessage.isNotEmpty()) {
                                    chatHistory += "我: $chatMessage\n\n"
                                    viewModel.chat(userId, chatMessage)
                                    chatMessage = ""
                                }
                            },
                            enabled = !isLoading && chatMessage.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Send, "发送")
                        }
                    }
                }
            }
            
            // 3. 食物推荐卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌟 食物推荐",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "根据您的健康状况推荐适合的饮品",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Button(
                        onClick = {
                            resultText = "⏳ 正在获取食物推荐..."
                            viewModel.getRecommendations(userId, "mixed", 5)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Recommend, "推荐")
                        Spacer(Modifier.width(4.dp))
                        Text("获取推荐")
                    }
                }
            }
            
            // 4. 健康分析卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "健康分析",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "查看您的健康数据分析报告",
                                fontSize = 13.sp,
                                color = Gray600
                            )
                        }
                    }
                    Button(
                        onClick = {
                            resultText = "正在分析健康数据..."
                            viewModel.getHealthAnalysis(userId, 7)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(3.dp)
                    ) {
                        Icon(Icons.Default.Analytics, "分析", modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("查看分析", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            // 5. 结果显示卡片 - 使用醒目的样式
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Gray100
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF66BB6A), Color(0xFF4CAF50))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "结果",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "分析结果",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    
                    // 添加日志跟踪渲染
                    Log.d("AIServiceScreen", "📺 UI正在渲染resultText,当前值前50字符: ${resultText.take(50)}")
                    
                    // 使用SelectionContainer让文本可选择,方便用户复制
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = resultText,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }
        }
    }
}

