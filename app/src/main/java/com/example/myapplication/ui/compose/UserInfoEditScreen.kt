package com.example.myapplication.ui.compose

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.Gender
import com.example.myapplication.model.UpdateUserInfoRequest
import com.example.myapplication.model.UserInfo
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.awaitResponse
import java.io.File
import java.io.FileOutputStream

/**
 * 用户信息编辑页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoEditScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // 状态
    var isLoading by remember { mutableStateOf(true) }
    var userInfo by remember { mutableStateOf<UserInfo?>(null) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("other") }
    var birthday by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showGenderPicker by remember { mutableStateOf(false) }
    @Suppress("UNUSED_VARIABLE")
    var showDatePicker by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    
    // 图库启动器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                selectedImageBitmap = bitmap
                
                // 上传头像
                scope.launch {
                    try {
                        isUploading = true
                        errorMessage = null
                        
                        // 转换为文件
                        val tempFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                        context.contentResolver.openInputStream(it)?.use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        // 创建MultipartBody.Part
                        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                        
                        // 上传
                        val response = RetrofitClient.getUserInfoApiService()
                            .uploadAvatar(filePart)
                            .awaitResponse()
                        
                        if (response.isSuccessful && response.body()?.isSuccess == true) {
                            avatarUrl = response.body()?.data
                            successMessage = "头像上传成功！"
                            // 3秒后清除成功消息
                            kotlinx.coroutines.delay(3000)
                            successMessage = null
                        } else {
                            errorMessage = "头像上传失败: ${response.body()?.message ?: "未知错误"}"
                        }
                        
                        // 清理临时文件
                        tempFile.delete()
                    } catch (e: Exception) {
                        errorMessage = "头像上传失败: ${e.message}"
                    } finally {
                        isUploading = false
                    }
                }
            } catch (e: Exception) {
                errorMessage = "图片加载失败: ${e.message}"
            }
        }
    }
    
    // 加载用户信息
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.getUserInfoApiService()
                    .getUserInfo()
                    .awaitResponse()
                
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val info = response.body()?.data
                    info?.let {
                        userInfo = it
                        username = it.username
                        email = it.email
                        phone = it.phone ?: ""
                        gender = it.gender ?: "other"
                        birthday = it.birthday ?: ""
                        avatarUrl = it.avatarUrl
                    }
                } else {
                    errorMessage = "加载用户信息失败: ${response.body()?.message ?: "未知错误"}"
                }
            } catch (e: Exception) {
                errorMessage = "加载用户信息失败: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    // 保存用户信息
    fun saveUserInfo() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                val request = UpdateUserInfoRequest(
                    username = if (username != userInfo?.username) username else null,
                    email = if (email != userInfo?.email) email else null,
                    phone = if (phone != userInfo?.phone) phone else null,
                    avatarUrl = null, // 头像已单独上传
                    gender = if (gender != userInfo?.gender) gender else null,
                    birthday = if (birthday.isNotEmpty() && birthday != userInfo?.birthday) birthday else null
                )
                
                val response = RetrofitClient.getUserInfoApiService()
                    .updateUserInfo(request)
                    .awaitResponse()
                
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    successMessage = "个人信息保存成功！"
                    // 3秒后清除成功消息
                    kotlinx.coroutines.delay(3000)
                    successMessage = null
                } else {
                    errorMessage = "保存失败: ${response.body()?.message ?: "未知错误"}"
                }
            } catch (e: Exception) {
                errorMessage = "保存失败: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑个人信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { saveUserInfo() },
                        enabled = !isLoading && !isUploading
                    ) {
                        Text("保存", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                    )
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && userInfo == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF4CAF50)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 成功提示
                    successMessage?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                                Text(msg, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                    
                    // 错误提示
                    errorMessage?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F))
                                Text(msg, color = Color(0xFFD32F2F))
                            }
                        }
                    }
                    
                    // 头像卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 头像预览
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0))
                                    .clickable { galleryLauncher.launch("image/*") }
                            ) {
                                if (selectedImageBitmap != null) {
                                    Image(
                                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                                        contentDescription = "头像",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (avatarUrl != null) {
                                    AsyncImage(
                                        model = "http://10.0.2.2:8080$avatarUrl",
                                        contentDescription = "头像",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "默认头像",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        tint = Color.Gray
                                    )
                                }
                                
                                // 上传中遮罩
                                if (isUploading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f))
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .align(Alignment.Center),
                                            color = Color.White
                                        )
                                    }
                                }
                                
                                // 相机图标
                                if (!isUploading) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoCamera,
                                            contentDescription = "更换头像",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.Center),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = "点击更换头像",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // 基本信息卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "基本信息",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            
                            // 用户名
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("用户名") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, "用户名")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // 邮箱
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("邮箱") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, "邮箱")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // 手机号
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("手机号") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, "手机号")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // 性别选择
                            OutlinedTextField(
                                value = Gender.fromValue(gender).displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("性别") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, "性别")
                                },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, "选择")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showGenderPicker = true }
                            )
                            
                            // 生日
                            OutlinedTextField(
                                value = birthday,
                                onValueChange = { birthday = it },
                                label = { Text("生日 (yyyy-MM-dd)") },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, "生日")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("1990-01-01") }
                            )
                        }
                    }
                    
                    // 账户信息卡片（只读）
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Gray100
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "账户信息",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            
                            userInfo?.let { info ->
                                UserInfoRow("用户ID", info.id.toString())
                                UserInfoRow("账户状态", when(info.status) {
                                    "active" -> "正常"
                                    "inactive" -> "未激活"
                                    "banned" -> "已封禁"
                                    else -> info.status ?: "未知"
                                })
                                info.createdAt?.let { UserInfoRow("注册时间", it) }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // 性别选择对话框
        if (showGenderPicker) {
            AlertDialog(
                onDismissRequest = { showGenderPicker = false },
                title = { Text("选择性别") },
                text = {
                    Column {
                        Gender.values().forEach { g ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gender = g.value
                                        showGenderPicker = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = gender == g.value,
                                    onClick = {
                                        gender = g.value
                                        showGenderPicker = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(g.displayName)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGenderPicker = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

/**
 * 用户信息行组件
 */
@Composable
private fun UserInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color(0xFF212121),
            fontSize = 14.sp
        )
    }
}

