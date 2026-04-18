package com.example.myapplication.ui.compose

import okhttp3.MediaType.Companion.toMediaType
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.viewmodel.AIServiceViewModel
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun RecognitionScreen(
    viewModel: AIServiceViewModel,
    mealViewModel: com.example.myapplication.viewmodel.LocalMealViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDiary: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1).toInt()

    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var flashOn by remember { mutableStateOf(true) }
    var showResult by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    var showModeSelector by remember { mutableStateOf(false) }
    var pendingImageFile by remember { mutableStateOf<File?>(null) }
    var showVitResult by remember { mutableStateOf(false) }
    var vitResult by remember { mutableStateOf<com.example.myapplication.model.FoodItem?>(null) }
    var vitLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val drinkRecognitionResult by viewModel.drinkRecognitionResult.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

    var showLocalFallback by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (!isLoading && showLoading) {
            showLoading = false
            if (drinkRecognitionResult?.isSuccess == true) {
                showResult = true
            } else if (errorMessage?.isNotEmpty() == true) {
                showLocalFallback = true
            }
        }
    }

    fun prepareImageFile(bitmap: Bitmap): File {
        val tempFile = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        return tempFile
    }

    fun clearAllResults() {
        showResult = false
        showVitResult = false
        vitResult = null
        showLocalFallback = false
    }

    fun startAiRecognition(file: File) {
        clearAllResults()
        showLoading = true
        showModeSelector = false
        viewModel.recognizeDrink(userId, file)
    }

    fun startVitRecognition(file: File) {
        clearAllResults()
        showModeSelector = false
        showLoading = true
        viewModel.recognizeDrink(userId, file)
    }

    var recognitionTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(recognitionTrigger) {
        if (recognitionTrigger > 0 && pendingImageFile != null) {
            startAiRecognition(pendingImageFile!!)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedImageBitmap = it
            try {
                pendingImageFile = prepareImageFile(it)
                recognitionTrigger++
            } catch (e: Exception) {
                Log.e("RecognitionScreen", "图片处理失败", e)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                selectedImageBitmap = bitmap
                val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                }
                if (tempFile.exists()) {
                    pendingImageFile = tempFile
                    recognitionTrigger++
                }
            } catch (e: Exception) {
                Log.e("RecognitionScreen", "图片加载失败", e)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    if (showResult && drinkRecognitionResult?.isSuccess == true) {
        RecognitionResultPage(
            result = drinkRecognitionResult!!,
            imageBitmap = selectedImageBitmap,
            mealViewModel = mealViewModel,
            onBack = { showResult = false },
            onSavedToDiary = {
                showResult = false
                onNavigateToDiary()
            }
        )
        return
    }

    if (showVitResult && vitResult != null) {
        VitRecognitionResultPage(
            foodItem = vitResult!!,
            imageBitmap = selectedImageBitmap,
            mealViewModel = mealViewModel,
            userId = userId,
            onBack = { showVitResult = false; vitResult = null },
            onSavedToDiary = {
                showVitResult = false
                vitResult = null
                onNavigateToDiary()
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Camera viewfinder area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Background gradient simulating camera
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF374151), Color(0xFF1F2937))
                        )
                    )
            )

            selectedImageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onNavigateBack() },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Close, "关闭", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Text(
                        "将食物/饮品放在画面中央",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { flashOn = !flashOn },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            if (flashOn) "⚡" else "🔦",
                            fontSize = 20.sp
                        )
                    }
                }
            }

            // Viewfinder frame
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(256.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                )
            }
        }

        // Bottom control area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 40.dp)
        ) {
            // Recent photo thumbnail
            if (selectedImageBitmap != null) {
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    selectedImageBitmap?.let { bitmap ->
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.2f))
                            )
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Text("已拍 1 张", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                }
            }

            // Main controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("相册", color = Color.White, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("相册", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                }

                // Shutter button
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) cameraLauncher.launch(null)
                            else permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 4.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MintGreen
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("📷", fontSize = 28.sp)
                            }
                        }
                    }
                }

                // Search entry
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onNavigateToSearch() }
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("搜索", color = Color.White, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("搜索添加", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                }
            }
        }
    } // end Column

        // Loading overlay
        if (showLoading || isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing)
                        ),
                        label = "rotation"
                    )

                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(80.dp)
                                .rotate(rotation),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            strokeWidth = 4.dp
                        )
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MintGreen.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("🤖", fontSize = 24.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("AI 正在识别中...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正在分析食物成分与糖分含量", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        }

        if (showLocalFallback && selectedImageBitmap != null) {
            LocalFoodMatchScreen(
                imageBitmap = selectedImageBitmap!!,
                mealViewModel = mealViewModel,
                onBack = { showLocalFallback = false },
                onSavedToDiary = {
                    showLocalFallback = false
                    onNavigateToDiary()
                }
            )
        }

        errorMessage?.takeIf { it.isNotEmpty() && !showLoading && !showLocalFallback }?.let { _ ->
            var showManualInput by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp, start = 24.dp, end = 24.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF333333).copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("识别暂时不可用", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("网络连接异常或服务维护中", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showManualInput = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                    ) {
                        Text("手动输入食物", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    TextButton(onClick = { onNavigateToSearch() }) {
                        Text("从食物库搜索添加", fontSize = 12.sp, color = MintGreen)
                    }
                }
            }

            if (showManualInput) {
                ManualFoodInputSheet(
                    mealViewModel = mealViewModel,
                    onDismiss = { showManualInput = false },
                    onSaved = {
                        showManualInput = false
                        Toast.makeText(context, "✅ 已添加到饮食日记", Toast.LENGTH_SHORT).show()
                        onNavigateToDiary()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualFoodInputSheet(
    mealViewModel: com.example.myapplication.viewmodel.LocalMealViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var foodName by remember { mutableStateOf("") }
    var sugarAmount by remember { mutableStateOf("") }
    var caloriesAmount by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf(com.example.myapplication.model.MealType.getCurrentMealType()) }
    val isLoading by mealViewModel.isLoading.observeAsState(false)
    val addSuccess by mealViewModel.addMealSuccess.observeAsState(false)
    var saveInitiated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mealViewModel.resetAddState()
    }

    LaunchedEffect(addSuccess) {
        if (addSuccess && saveInitiated) onSaved()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text("手动添加食物", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Spacer(modifier = Modifier.height(20.dp))

            Text("食物名称", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = foodName, onValueChange = { foodName = it },
                placeholder = { Text("如：茉莉奶绿(三分糖)", color = Color(0xFFD1D5DB)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("含糖量 (克)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = sugarAmount, onValueChange = { sugarAmount = it },
                placeholder = { Text("参考包装标签", color = Color(0xFFD1D5DB)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("热量 (kcal，选填)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = caloriesAmount, onValueChange = { caloriesAmount = it },
                placeholder = { Text("可留空", color = Color(0xFFD1D5DB)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("请选择餐次", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.example.myapplication.model.MealType.values().forEach { mt ->
                    val isSelected = selectedMealType == mt
                    Button(
                        onClick = { selectedMealType = mt },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MintGreen else Color(0xFFF9FAFB),
                            contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text(mt.displayName, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (foodName.isBlank() || sugarAmount.isBlank()) {
                        Toast.makeText(context, "请填写食物名称和含糖量", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    saveInitiated = true
                    val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                    val uid = prefs.getLong("user_id", 1).toInt()
                    mealViewModel.addMeal(
                        userId = uid, foodName = foodName,
                        sugarContent = sugarAmount.toDoubleOrNull() ?: 0.0,
                        calories = caloriesAmount.toDoubleOrNull() ?: 0.0,
                        protein = null, fat = null, carbohydrate = null,
                        portionSize = null, notes = "手动输入",
                        mealType = selectedMealType.value, imageUrl = null
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(6.dp),
                enabled = !isLoading && foodName.isNotBlank() && sugarAmount.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("保存到日记", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RecognitionResultPage(
    result: com.example.myapplication.model.DrinkRecognitionResponse,
    imageBitmap: Bitmap?,
    mealViewModel: com.example.myapplication.viewmodel.LocalMealViewModel,
    onBack: () -> Unit,
    onSavedToDiary: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSaveDialog by remember { mutableStateOf(false) }
    var showCorrectDialog by remember { mutableStateOf(false) }
    var savedImageFile by remember { mutableStateOf<File?>(null) }
    var showDetailNutrition by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1).toInt()
    var todaySugar by remember { mutableDoubleStateOf(0.0) }
    var sugarLimit by remember { mutableFloatStateOf(25f) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val mealResp = com.example.myapplication.api.RetrofitClient.getMealApiService()
                        .getDailyMeals(userId, today).execute()
                    if (mealResp.isSuccessful && mealResp.body()?.isSuccess == true) {
                        val data = mealResp.body()!!.data
                        todaySugar = (data?.get("total_sugar") as? Number)?.toDouble() ?: 0.0
                    }
                    val profileResp = com.example.myapplication.api.RetrofitClient.getUserProfileApiService()
                        .getHealthProfile().execute()
                    if (profileResp.isSuccessful && profileResp.body()?.isSuccess == true) {
                        sugarLimit = profileResp.body()!!.data?.sugarLimit ?: 25f
                    }
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(imageBitmap) {
        imageBitmap?.let { bitmap ->
            try {
                val file = File(context.cacheDir, "meal_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.close()
                savedImageFile = file
            } catch (e: Exception) {
                Log.e("RecognitionResultPage", "保存图片失败", e)
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = Color(0xFF757575), modifier = Modifier.size(20.dp))
                }
            }
            Text("识别结果", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Spacer(modifier = Modifier.width(40.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(192.dp)
                    ) {
                        imageBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📷", fontSize = 40.sp)
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        result.recognition?.let { recognition ->
                            Text(
                                recognition.drinkName ?: "未知食物",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                "置信度 ${(recognition.confidence * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = Color(0xFFBDBDBD),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        result.nutrition?.let { nutrition ->
                            val sugarVal = nutrition.sugarContent
                            val isHighSugar = sugarVal > 20
                            val sugarColor = if (isHighSugar) Color(0xFFEF4444) else MintGreen
                            val sugarBg = if (isHighSugar) Color(0xFFFEF2F2) else MintBg
                            val percentage = if (sugarLimit > 0) ((sugarVal / sugarLimit) * 100).toInt() else 0

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = sugarBg
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            "${sugarVal.toInt()}",
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Black,
                                            color = sugarColor
                                        )
                                        Text(
                                            "g 含糖量",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = sugarColor.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                        )
                                    }
                                    Text(
                                        "约占每日推荐上限(${sugarLimit.toInt()}g)的 ${percentage}%",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val bowls = sugarVal / 30.0
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF9FAFB)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("含糖量相当于", fontSize = 12.sp, color = Color(0xFF6B7280))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🍚", fontSize = 20.sp)
                                        Text("×", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                        Text(
                                            String.format("%.1f", bowls),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = sugarColor
                                        )
                                        Text("碗", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                    }
                                    Text(
                                        "≈ ${String.format("%.1f", bowls)} 碗大米饭的碳水含量",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF374151),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        "一碗米饭（150g）含碳水约30g",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val totalAfter = todaySugar + sugarVal
                            val overAmount = totalAfter - sugarLimit
                            if (overAmount > 0 || totalAfter > sugarLimit * 0.8) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFF7ED),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFEDD5))
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("ℹ️", fontSize = 14.sp)
                                        Text(
                                            buildString {
                                                append("今日已摄入 ${todaySugar.toInt()}g，加上这份将达 ${totalAfter.toInt()}g。")
                                                if (overAmount > 0) {
                                                    append("超出目标 ${overAmount.toInt()}g，但也不用太紧张，明天少吃一些就追回来了 \uD83D\uDE0A")
                                                } else {
                                                    append("接近目标上限，注意控制哦 \uD83D\uDE0A")
                                                }
                                            },
                                            fontSize = 12.sp,
                                            color = Color(0xFFEA580C),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDetailNutrition = !showDetailNutrition },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFAFAFA)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("查看详细营养信息", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B5563))
                                        Icon(
                                            if (showDetailNutrition) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color(0xFFBDBDBD)
                                        )
                                    }
                                    if (showDetailNutrition) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            NutritionBox("${nutrition.calories.toInt()}", "热量 kcal")
                                            NutritionBox("${(nutrition.sugarContent * 1.2).toInt()}", "碳水 g")
                                            NutritionBox("${(nutrition.calories * 0.03).toInt()}", "脂肪 g")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            result.healthAssessment?.let { assessment ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🤖", fontSize = 18.sp)
                            Text("AI 营养分析", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            assessment.healthAdvice ?: "这份食物的糖分含量较高，建议适量食用。搭配蛋白质食物一起吃，可以让血糖升得更平稳。",
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MintBg
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("聪明喝法（推荐试试）", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MintGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                val tips = listOf(
                                    "搭配蛋白质（鸡蛋、坚果）一起吃，能让血糖升得更平稳",
                                    "饭后半小时喝比空腹喝好很多，对身体更友好",
                                    "下次试试少糖版本？口感差别不大，含糖量会降低不少"
                                )
                                tips.forEach { tip ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("✨", fontSize = 12.sp, color = MintGreen)
                                        Text(tip, fontSize = 12.sp, color = Color(0xFF4B5563), lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showCorrectDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F4F6),
                        contentColor = Color(0xFF6B7280)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("修正结果", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    elevation = ButtonDefaults.buttonElevation(6.dp)
                ) {
                    Text("加入日记", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveToMealDiarySheet(
            result = result,
            imageFile = savedImageFile,
            mealViewModel = mealViewModel,
            onDismiss = { showSaveDialog = false },
            onSaved = {
                showSaveDialog = false
                Toast.makeText(context, "✅ 已保存到今日日记", Toast.LENGTH_SHORT).show()
                onSavedToDiary()
            }
        )
    }

    if (showCorrectDialog) {
        CorrectResultSheet(
            result = result,
            onDismiss = { showCorrectDialog = false },
            onSave = { name, sugar ->
                result.recognition?.drinkName = name
                result.nutrition?.sugarContent = sugar
                showCorrectDialog = false
                Toast.makeText(context, "修正已保存", Toast.LENGTH_SHORT).show()
            },
            onFeedback = {
                showCorrectDialog = false
                Toast.makeText(context, "反馈已提交，感谢！", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun NutritionBox(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF9FAFB)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Text(label, fontSize = 10.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF757575))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = Color(0xFF2E7D32))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B5E20))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveToMealDiarySheet(
    result: com.example.myapplication.model.DrinkRecognitionResponse,
    imageFile: File?,
    mealViewModel: com.example.myapplication.viewmodel.LocalMealViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var selectedMealType by remember { mutableStateOf<com.example.myapplication.model.MealType?>(null) }
    var selectedPortion by remember { mutableStateOf("full") }
    val aiAdviceText = result.healthAssessment?.healthAdvice ?: result.recommendation
    val defaultNotesPreview = aiAdviceText?.take(100) ?: ""
    var notes by remember { mutableStateOf(defaultNotesPreview) }
    var foodName by remember { mutableStateOf(result.recognition?.drinkName ?: "未知食物") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var customMode by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    var saveInitiated by remember { mutableStateOf(false) }

    val baseSugar = (result.nutrition?.sugarContent ?: 0f).toDouble()
    val baseCalories = (result.nutrition?.calories ?: 0f).toDouble()

    val currentMultiplier = if (customMode) {
        val inputVal = customInput.toDoubleOrNull() ?: 0.0
        if (inputVal > 0) inputVal / 100.0 else 1.0
    } else {
        portionMultiplier(selectedPortion)
    }

    val adjustedSugar = baseSugar * currentMultiplier
    val adjustedCalories = baseCalories * currentMultiplier

    val isLoading by mealViewModel.isLoading.observeAsState(false)
    val addSuccess by mealViewModel.addMealSuccess.observeAsState(false)

    LaunchedEffect(Unit) {
        mealViewModel.resetAddState()
    }

    LaunchedEffect(addSuccess) {
        if (addSuccess && saveInitiated) {
            onSaved()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("加入饮食日记", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "关闭", tint = Color(0xFF9CA3AF))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("食物名称", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = foodName,
                onValueChange = { foodName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF9FAFB)
            ) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${adjustedSugar.toInt()}g 糖 · ${adjustedCalories.toInt()} kcal",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen
                    )
                    if (currentMultiplier != 1.0) {
                        Text(
                            "（原 ${baseSugar.toInt()}g · ${baseCalories.toInt()}kcal）",
                            fontSize = 11.sp, color = Color(0xFFBDBDBD)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("请选择餐次", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.example.myapplication.model.MealType.values().forEach { mt ->
                    val isSelected = selectedMealType == mt
                    Button(
                        onClick = { selectedMealType = mt },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MintGreen else Color(0xFFF9FAFB),
                            contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(mt.displayName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("份量", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                portionOptions.forEach { (value, label) ->
                    val isSelected = !customMode && selectedPortion == value
                    Button(
                        onClick = { selectedPortion = value; customMode = false },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MintGreen else Color(0xFFF9FAFB),
                            contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { customMode = !customMode },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (customMode) MintGreen else Color(0xFFF9FAFB),
                    contentColor = if (customMode) Color.White else Color(0xFF6B7280)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("自定义分量", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            if (customMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入分量") },
                    suffix = { Text("g", color = Color(0xFF9CA3AF)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintGreen,
                        cursorColor = MintGreen
                    )
                )
                Text(
                    "以100g为标准份量计算",
                    fontSize = 10.sp, color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = 4.dp)
                )
                val customVal = customInput.toDoubleOrNull()
                if (customVal != null && customVal > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MintBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("对应糖分", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${adjustedSugar.toInt()}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                            Text("热量", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${adjustedCalories.toInt()}kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("备注（可选）", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：半糖、去冰、社交豁免日...", color = Color(0xFFD1D5DB), fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("标签（可选）", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            val tags = listOf("社交饮品", "酱汁类", "代糖饮品", "练后补水")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    val isSelected = selectedTags.contains(tag)
                    Surface(
                        modifier = Modifier.clickable {
                            selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MintBg else Color(0xFFF9FAFB),
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))
                        ) else null
                    ) {
                        Text(
                            tag, fontSize = 12.sp,
                            color = if (isSelected) MintGreen else Color(0xFF6B7280),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val canSave = selectedMealType != null && foodName.isNotBlank() && !isLoading &&
                    (!customMode || (customInput.toDoubleOrNull() ?: 0.0) > 0)
            val portionDesc = if (customMode) "${customInput}g" else portionDisplayName(selectedPortion)

            Button(
                onClick = {
                    val mt = selectedMealType ?: return@Button
                    result.nutrition?.let { _ ->
                        saveInitiated = true
                        val prefs2 = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                        val uid = prefs2.getLong("user_id", 1).toInt()
                        val userKeptDefault = aiAdviceText != null &&
                            (notes == defaultNotesPreview || notes == aiAdviceText)
                        val finalNotes = buildString {
                            append("AI识别")
                            if (userKeptDefault && aiAdviceText != null) {
                                append(" | ${aiAdviceText}")
                            } else if (notes.isNotEmpty()) {
                                append(" | ${notes}")
                            }
                            if (selectedTags.isNotEmpty()) {
                                append(" | ${selectedTags.joinToString(", ")}")
                            }
                        }
                        mealViewModel.addMeal(
                            userId = uid,
                            foodName = foodName,
                            sugarContent = adjustedSugar,
                            calories = adjustedCalories,
                            protein = null, fat = null, carbohydrate = null,
                            portionSize = portionDesc,
                            notes = finalNotes.ifEmpty { null },
                            mealType = mt.value,
                            imageUrl = imageFile?.absolutePath
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(6.dp),
                enabled = canSave
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("保存到日记", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrectResultSheet(
    result: com.example.myapplication.model.DrinkRecognitionResponse,
    onDismiss: () -> Unit,
    onSave: (String, Float) -> Unit,
    onFeedback: () -> Unit
) {
    var foodName by remember { mutableStateOf(result.recognition?.drinkName ?: "") }
    var sugarAmount by remember { mutableStateOf("${result.nutrition?.sugarContent?.toInt() ?: 0}") }
    var calories by remember { mutableStateOf("${result.nutrition?.calories?.toInt() ?: 0}") }
    var reason by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("修正识别结果", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "关闭", tint = Color(0xFF9CA3AF))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEFCE8)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠️", fontSize = 14.sp)
                    Text(
                        "如果识别结果不准确，请在下方修正。你的反馈将帮助我们改进AI模型。",
                        fontSize = 12.sp, color = Color(0xFFA16207), lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("食物/饮品名称", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = foodName, onValueChange = { foodName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("含糖量 (克)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = sugarAmount, onValueChange = { sugarAmount = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true
            )
            Text("请参考包装标签或官方营养信息", fontSize = 10.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 4.dp))

            Spacer(modifier = Modifier.height(12.dp))

            Text("热量 (kcal)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = calories, onValueChange = { calories = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("修正原因（可选）", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = reason, onValueChange = { reason = it },
                modifier = Modifier.fillMaxWidth().height(72.dp),
                placeholder = { Text("如：包装标注为32g / AI识别错误品牌...", color = Color(0xFFD1D5DB), fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onFeedback,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color(0xFF6B7280)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("提交反馈", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Button(
                    onClick = { onSave(foodName, sugarAmount.toFloatOrNull() ?: 0f) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    elevation = ButtonDefaults.buttonElevation(6.dp)
                ) {
                    Text("保存修正", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalFoodMatchScreen(
    imageBitmap: Bitmap,
    mealViewModel: com.example.myapplication.viewmodel.LocalMealViewModel,
    onBack: () -> Unit,
    onSavedToDiary: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var drinksList by remember { mutableStateOf(listOf<com.example.myapplication.model.Drink>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showSaveSheet by remember { mutableStateOf(false) }
    var selectedName by remember { mutableStateOf("") }
    var selectedSugar by remember { mutableStateOf(0f) }
    var selectedCalories by remember { mutableStateOf(0f) }

    var mlKitResults by remember { mutableStateOf(listOf<String>()) }
    var isRecognizing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                mlKitResults = com.example.myapplication.util.LocalFoodRecognizer.recognizeFood(imageBitmap)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val resp = com.example.myapplication.api.RetrofitClient.getDrinkApiService()
                        .getAllDrinks().execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                        drinksList = resp.body()!!.data ?: emptyList()
                    }
                }
            } catch (_: Exception) {}
            isRecognizing = false
        }
    }

    val allItems = remember(drinksList, searchQuery, mlKitResults) {
        val all = drinksList.map { Triple(it.drinkName ?: "未知饮品", it.sugarContent ?: 0f, it.calories ?: 0f) }
        val filtered = if (searchQuery.isNotBlank()) {
            all.filter { it.first.contains(searchQuery, ignoreCase = true) }
        } else if (mlKitResults.isNotEmpty()) {
            val matched = all.filter { item -> mlKitResults.any { label -> item.first.contains(label) || label.contains(item.first) } }
            if (matched.isNotEmpty()) matched + all.filter { it !in matched }.take(20)
            else all
        } else all
        filtered
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))) {
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(40.dp).clickable { onBack() },
                shape = CircleShape, color = Color.White, shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = Color(0xFF757575), modifier = Modifier.size(20.dp))
                }
            }
            Text("选择食物", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Spacer(modifier = Modifier.width(40.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(bitmap = imageBitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Column {
                    if (isRecognizing) {
                        Text("🤖 AI正在识别...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        Text("正在分析图片中的食物", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    } else {
                        Text("🔍 识别完成", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        if (mlKitResults.isNotEmpty()) {
                            Text("可能是: ${mlKitResults.take(3).joinToString(", ")}", fontSize = 12.sp, color = Color(0xFF6B7280))
                        } else {
                            Text("请从下方选择匹配的食物", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text("搜索食物/饮品...", color = Color(0xFFD1D5DB)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFBDBDBD)) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allItems.size) { index ->
                val (name, sugar, cal) = allItems[index]
                val sugarColor = when { sugar > 20 -> Color(0xFFEF4444); sugar > 10 -> Color(0xFFFF9800); else -> MintGreen }
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedName = name; selectedSugar = sugar; selectedCalories = cal
                        showSaveSheet = true
                    },
                    shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(10.dp), color = Color(0xFFF5F5F5)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(name.firstOrNull()?.toString() ?: "🍽", fontSize = 20.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                            Text("${cal.toInt()} kcal", fontSize = 11.sp, color = Color(0xFFBDBDBD))
                        }
                        Text("${sugar.toInt()}g", fontSize = 16.sp, fontWeight = FontWeight.Black, color = sugarColor)
                    }
                }
            }
        }
    }

    if (showSaveSheet) {
        val result = com.example.myapplication.model.DrinkRecognitionResponse().apply {
            setSuccess(true)
            recognition = com.example.myapplication.model.DrinkRecognitionResponse.Recognition().apply { drinkName = selectedName; confidence = 0.95f }
            nutrition = com.example.myapplication.model.DrinkRecognitionResponse.Nutrition().apply { sugarContent = selectedSugar; calories = selectedCalories; healthScore = if (selectedSugar < 10) 80 else if (selectedSugar < 25) 50 else 25 }
            healthAssessment = com.example.myapplication.model.DrinkRecognitionResponse.HealthAssessment().apply {
                healthAdvice = if (selectedSugar > 25) "这款食物糖分较高，建议控制摄入量。" else "这款食物糖分适中，是不错的选择！"
            }
        }
        SaveToMealDiarySheet(result = result, imageFile = null, mealViewModel = mealViewModel,
            onDismiss = { showSaveSheet = false },
            onSaved = { showSaveSheet = false; Toast.makeText(context, "✅ 已保存到今日日记", Toast.LENGTH_SHORT).show(); onSavedToDiary() })
    }
}

@Composable
private fun VitRecognitionResultPage(
    foodItem: com.example.myapplication.model.FoodItem,
    imageBitmap: Bitmap?,
    mealViewModel: com.example.myapplication.viewmodel.LocalMealViewModel,
    userId: Int,
    onBack: () -> Unit,
    onSavedToDiary: () -> Unit
) {
    val context = LocalContext.current
    val sugar = foodItem.sugar ?: 0f
    val calories = foodItem.calories ?: 0f
    val confidence = (foodItem.confidence ?: 0f) * 100f
    val healthLevel = foodItem.healthLevel ?: "moderate"
    val riceEquiv = if (sugar > 0) String.format("%.1f", sugar / 30f) else "0"

    var sugarLimit by remember { mutableFloatStateOf(25f) }
    var todaySugar by remember { mutableFloatStateOf(0f) }
    var aiAdvice by remember { mutableStateOf("") }
    var aiLoading by remember { mutableStateOf(false) }
    var showVitSaveSheet by remember { mutableStateOf(false) }
    val initialFoodName = foodItem.nameCN?.takeIf { it.isNotBlank() }
        ?: foodItem.nameEN?.takeIf { it.isNotBlank() }
        ?: "未知食物"
    var editableFoodName by remember(foodItem.nameCN) { mutableStateOf(initialFoodName) }
    val isLikelyUnknown =
        editableFoodName.contains("未知", ignoreCase = true)
            || editableFoodName.matches(Regex("^[a-zA-Z\\s]+$"))
            || (foodItem.confidence ?: 0f) < 0.25f

    var savedImagePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageBitmap) {
        imageBitmap?.let { bitmap ->
            try {
                val dir = File(context.filesDir, "meal_images")
                dir.mkdirs()
                val file = File(dir, "vit_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) }
                savedImagePath = file.absolutePath
            } catch (_: Exception) {}
        }
    }

    var userProfileInfo by remember { mutableStateOf("") }
    var recentHealthSummary by remember { mutableStateOf("") }
    var drinkPrefSummary by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val profileResp = com.example.myapplication.api.RetrofitClient.getUserProfileApiService().getHealthProfile().execute()
                if (profileResp.isSuccessful && profileResp.body()?.isSuccess == true) {
                    val p = profileResp.body()?.data
                    sugarLimit = p?.sugarLimit ?: 25f
                    if (p != null) {
                        val genderStr = when (p.gender) { "male" -> "男"; "female" -> "女"; else -> p.gender }
                        val actStr = when (p.activityLevel) { "sedentary" -> "久坐"; "light" -> "轻度活动"; "moderate" -> "中度活动"; "active" -> "活跃"; "very_active" -> "非常活跃"; else -> p.activityLevel ?: "未知" }
                        userProfileInfo = buildString {
                            append("用户档案：${p.age}岁${genderStr}，身高${p.height?.toInt()}cm，体重${p.weight?.toInt()}kg，活动水平${actStr}")
                            if (!p.healthConditions.isNullOrBlank()) append("，健康状况：${p.healthConditions}")
                            if (!p.allergies.isNullOrBlank()) append("，过敏：${p.allergies}")
                            if (!p.medications.isNullOrBlank()) append("，用药：${p.medications}")
                            append("，每日糖分目标${sugarLimit.toInt()}g，热量目标${p.calorieLimit?.toInt() ?: 2000}kcal")
                        }
                    }
                }
                val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val mealResp = com.example.myapplication.api.RetrofitClient.getMealApiService().getDailyMeals(userId, today).execute()
                if (mealResp.isSuccessful && mealResp.body()?.isSuccess == true) {
                    val data = mealResp.body()!!.data
                    todaySugar = (data?.get("total_sugar") as? Number)?.toFloat() ?: 0f
                    val totalCal = (data?.get("total_calories") as? Number)?.toFloat() ?: 0f
                    val mealCount = (data?.get("meal_count") as? Number)?.toInt() ?: 0
                    recentHealthSummary = "今日已摄入糖分${todaySugar.toInt()}g，热量${totalCal.toInt()}kcal，已记录${mealCount}餐"
                }
                try {
                    val recentResp = com.example.myapplication.api.RetrofitClient.getDailyHealthRecordApiService().getRecentRecords(3).execute()
                    if (recentResp.isSuccessful && recentResp.body()?.isSuccess == true) {
                        val records = recentResp.body()?.data
                        if (!records.isNullOrEmpty()) {
                            val avgSugar = records.mapNotNull { it.totalSugarIntake }.average()
                            val avgCal = records.mapNotNull { it.totalCalories }.average()
                            recentHealthSummary += "。近3日平均糖分${avgSugar.toInt()}g，热量${avgCal.toInt()}kcal"
                        }
                    }
                } catch (_: Exception) {}
                try {
                    val prefResp = com.example.myapplication.api.RetrofitClient.getDrinkPreferenceApiService().getUserPreferences().execute()
                    if (prefResp.isSuccessful && prefResp.body()?.isSuccess == true) {
                        val prefs2 = prefResp.body()?.data
                        if (!prefs2.isNullOrEmpty()) {
                            val topPrefs = prefs2.sortedByDescending { it.preferenceScore ?: 0 }.take(5)
                            drinkPrefSummary = "用户饮品偏好：" + topPrefs.joinToString("、") { "${it.drinkName ?: "未知"}(偏好${it.preferenceScore}/5)" }
                        }
                    }
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
        aiLoading = true
        withContext(Dispatchers.IO) {
            try {
                val prompt = buildString {
                    append("你是控糖营养顾问。")
                    if (userProfileInfo.isNotBlank()) append(userProfileInfo).append("。")
                    append("用户识别到食物「${foodItem.nameCN}」，含糖${sugar}g，热量${calories}kcal，蛋白质${foodItem.protein}g，脂肪${foodItem.fat}g，碳水${foodItem.carbohydrate}g，健康等级${healthLevel}。")
                    if (recentHealthSummary.isNotBlank()) append(recentHealthSummary).append("。")
                    if (drinkPrefSummary.isNotBlank()) append(drinkPrefSummary).append("。")
                    append("请结合用户的个人健康档案和近期饮食数据，用3-4句话给出针对性的营养分析和饮食建议，语气温和鼓励。不要使用markdown格式。")
                }
                val resp = com.example.myapplication.api.RetrofitClient.getAIApiService()
                    .chat(com.example.myapplication.model.ChatRequest(userId, prompt, false)).execute()
                val raw = if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    resp.body()?.data?.response
                } else null
                withContext(Dispatchers.Main) {
                    aiAdvice = raw?.replace("**", "")?.replace("##", "")?.replace("# ", "")?.trim() ?: ""
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { aiAdvice = foodItem.healthAdvice ?: "" }
            }
            withContext(Dispatchers.Main) { aiLoading = false }
        }
    }

    val sugarColor = when {
        sugar <= 5 -> Color(0xFF2E7D32)
        sugar <= 15 -> MintGreen
        sugar <= 25 -> Color(0xFFFFA726)
        else -> Color(0xFFE53935)
    }
    val sugarBgColor = when {
        sugar <= 5 -> Color(0xFFE8F5E9)
        sugar <= 15 -> MintBg
        sugar <= 25 -> Color(0xFFFFF3E0)
        else -> Color(0xFFFFEBEE)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp).clickable { onBack() }, shape = CircleShape, color = Color.White, shadowElevation = 1.dp) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = Color(0xFF666666), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Text("识别结果", fontWeight = FontWeight.Bold, color = Color(0xFF333333), fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MintBg) {
                Box(contentAlignment = Alignment.Center) { Text("🔬", fontSize = 16.sp) }
            }
        }
        Spacer(Modifier.height(12.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column {
                    when {
                        imageBitmap != null -> {
                            val bmp = imageBitmap
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        !foodItem.imageUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(foodItem.imageUrl).crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📷", fontSize = 48.sp)
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(editableFoodName.ifBlank { "未知食物" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        val subtitleParts = listOfNotNull(
                            foodItem.nameEN?.takeIf { it.isNotBlank() && it != editableFoodName },
                            foodItem.category?.takeIf { it.isNotBlank() }
                        )
                        Text(
                            (subtitleParts + "置信度 ${String.format("%.0f", confidence)}%").joinToString(" · "),
                            fontSize = 12.sp, color = Color(0xFFBDBDBD)
                        )
                        Spacer(Modifier.height(12.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = sugarBgColor) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Bottom) {
                                Text("${sugar.toInt()}", fontSize = 36.sp, fontWeight = FontWeight.Black, color = sugarColor)
                                Spacer(Modifier.width(4.dp))
                                Text("g 含糖量", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = sugarColor.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 4.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("约占每日推荐上限(${sugarLimit.toInt()}g)的 ${(sugar / sugarLimit * 100).toInt()}%", fontSize = 10.sp, color = Color(0xFFBDBDBD))
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("含糖量相当于", fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🍚", fontSize = 28.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("×", fontSize = 14.sp, color = Color(0xFFBDBDBD))
                        Spacer(Modifier.width(8.dp))
                        Text(riceEquiv, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = sugarColor)
                        Spacer(Modifier.width(8.dp))
                        Text("碗", fontSize = 14.sp, color = Color(0xFF666666))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("≈ $riceEquiv 碗大米饭的碳水含量", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                    Text("一碗米饭（150g）含碳水约30g", fontSize = 10.sp, color = Color(0xFFBDBDBD))
                }
            }

            val totalAfter = todaySugar + sugar
            val overAmount = totalAfter - sugarLimit
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = if (overAmount > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9), border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(if (overAmount > 0) Color(0xFFFFE0B2) else Color(0xFFC8E6C9)))) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Text(if (overAmount > 0) "⚠️" else "✅", fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (overAmount > 0) "今日已摄入 ${todaySugar.toInt()}g，加上这份将达 ${totalAfter.toInt()}g。超出目标 ${overAmount.toInt()}g，但也不用太紧张，明天少吃一点就追回来了"
                        else "今日已摄入 ${todaySugar.toInt()}g，加上这份共 ${totalAfter.toInt()}g，仍在目标 ${sugarLimit.toInt()}g 内，继续保持！",
                        fontSize = 12.sp, color = if (overAmount > 0) Color(0xFFE65100) else Color(0xFF2E7D32), lineHeight = 18.sp
                    )
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("详细营养信息", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("${calories.toInt()}" to "热量 kcal", "${foodItem.carbohydrate?.toInt() ?: 0}" to "碳水 g", "${foodItem.fat?.toInt() ?: 0}" to "脂肪 g").forEach { (value, label) ->
                            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = Color(0xFFF5F5F5)) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                                    Text(label, fontSize = 10.sp, color = Color(0xFFBDBDBD))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("${foodItem.protein?.toInt() ?: 0}" to "蛋白质 g", "${foodItem.servingSize?.toInt() ?: 0}" to "份量 g").forEach { (value, label) ->
                            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = Color(0xFFF5F5F5)) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                                    Text(label, fontSize = 10.sp, color = Color(0xFFBDBDBD))
                                }
                            }
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("AI 营养分析", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                        if (aiLoading) {
                            Spacer(Modifier.width(8.dp))
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = MintGreen, strokeWidth = 2.dp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (aiAdvice.isNotBlank()) {
                        Text(aiAdvice, fontSize = 13.sp, color = Color(0xFF666666), lineHeight = 20.sp, modifier = Modifier.fillMaxWidth())
                    } else if (!aiLoading) {
                        val defaultAdvice = when (healthLevel) {
                            "healthy" -> "这是一种健康的食物选择！含糖量较低，适合日常食用。建议搭配均衡的其他营养素，保持健康饮食习惯。"
                            "moderate" -> "这种食物营养均衡，适量食用即可。注意控制整体摄入量，搭配蔬菜水果效果更佳。"
                            else -> "这种食物含糖量较高，建议控制食用频率和份量。可以搭配低糖食物来平衡一天的糖分摄入。"
                        }
                        Text(defaultAdvice, fontSize = 13.sp, color = Color(0xFF666666), lineHeight = 20.sp)
                    } else {
                        Text("正在为您生成个性化分析...", fontSize = 13.sp, color = Color(0xFFBDBDBD))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen))
                ) { Text("重新识别", color = MintGreen, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { showVitSaveSheet = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                ) { Text("加入日记", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
        }
    }

    if (showVitSaveSheet) {
        val notePreview = buildString {
            append("ViT模型识别")
            if (aiAdvice.isNotBlank()) {
                append(" | AI建议：")
                append(aiAdvice)
            }
        }

        var vitSaveInitiated by remember { mutableStateOf(false) }
        val vitAddSuccess by mealViewModel.addMealSuccess.observeAsState(false)
        val vitErrorMsg by mealViewModel.errorMessage.observeAsState("")

        LaunchedEffect(Unit) {
            mealViewModel.resetAddState()
        }

        LaunchedEffect(vitAddSuccess) {
            if (vitAddSuccess && vitSaveInitiated) {
                Toast.makeText(context, "已保存到今日日记", Toast.LENGTH_SHORT).show()
                showVitSaveSheet = false
                onSavedToDiary()
            }
        }

        LaunchedEffect(vitErrorMsg) {
            if (vitSaveInitiated && vitErrorMsg.isNotBlank() && !vitErrorMsg.contains("成功")) {
                Toast.makeText(context, vitErrorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        VitFoodSaveBottomSheet(
            foodName = editableFoodName,
            onFoodNameChange = { editableFoodName = it },
            showNameEditor = isLikelyUnknown,
            sugar = sugar.toDouble(),
            calories = calories.toDouble(),
            previewBitmap = imageBitmap,
            previewImageUrl = foodItem.imageUrl,
            defaultNotes = notePreview,
            servingSize = (foodItem.servingSize ?: 0f).toDouble(),
            servingSizeUnit = "g",
            isSaving = vitSaveInitiated,
            onDismiss = { showVitSaveSheet = false },
            onSave = { mealType, portionSize, notes, name, multiplier ->
                if (vitSaveInitiated) return@VitFoodSaveBottomSheet
                vitSaveInitiated = true
                mealViewModel.addMeal(
                    userId = userId,
                    foodName = name.ifBlank { foodItem.nameCN ?: foodItem.nameEN ?: "未知食物" },
                    sugarContent = sugar.toDouble() * multiplier,
                    calories = calories.toDouble() * multiplier,
                    protein = foodItem.protein?.toDouble()?.let { it * multiplier },
                    fat = foodItem.fat?.toDouble()?.let { it * multiplier },
                    carbohydrate = foodItem.carbohydrate?.toDouble()?.let { it * multiplier },
                    portionSize = portionSize,
                    notes = notes,
                    mealType = mealType,
                    imageUrl = savedImagePath
                )
            }
        )
    }
    }
}
