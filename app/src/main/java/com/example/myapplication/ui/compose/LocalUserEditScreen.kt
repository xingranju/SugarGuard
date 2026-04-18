package com.example.myapplication.ui.compose

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.UpdateUserInfoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalUserEditScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var birthday by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "avatar_${userId}.jpg")
                inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                avatarUrl = file.absolutePath
                localAvatarUri = Uri.fromFile(file)
            } catch (_: Exception) {
                Toast.makeText(context, "头像设置失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    val resp = RetrofitClient.getUserInfoApiService().getUserInfo().execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
                }
                if (user != null) {
                    nickname = user.username
                    email = user.email
                    phone = user.phone ?: ""
                    gender = user.gender ?: "male"
                    birthday = user.birthday ?: ""
                    avatarUrl = user.avatarUrl ?: ""
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color(0xFF333333))
                }
                Text(
                    "编辑资料",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                val request = UpdateUserInfoRequest(
                                    username = nickname.trim().ifBlank { null },
                                    email = email.trim().ifBlank { null },
                                    phone = phone.trim().ifBlank { null },
                                    gender = gender,
                                    birthday = birthday.trim().ifBlank { null },
                                    avatarUrl = avatarUrl.ifBlank { null }
                                )
                                val result = withContext(Dispatchers.IO) {
                                    RetrofitClient.getUserInfoApiService().updateUserInfo(request).execute()
                                }
                                if (result.isSuccessful && result.body()?.isSuccess == true) {
                                    val updatedName = result.body()?.data?.username ?: nickname
                                    prefs.edit().putString("username", updatedName).apply()
                                    Toast.makeText(context, "资料已更新", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "保存失败: ${result.body()?.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (_: Exception) {
                                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("保存", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
                ) {
                    val avatarModel = localAvatarUri ?: avatarUrl.ifBlank { null }
                    if (avatarModel != null) {
                        AsyncImage(
                            model = avatarModel,
                            contentDescription = "头像",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MintBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nickname.firstOrNull()?.toString() ?: "U",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintGreen
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp),
                        shape = CircleShape,
                        color = MintGreen,
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                EditField(
                    label = "昵称",
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "输入昵称"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "性别",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GenderChip("male", "男", gender == "male", Modifier.weight(1f)) { gender = "male" }
                    GenderChip("female", "女", gender == "female", Modifier.weight(1f)) { gender = "female" }
                }

                Spacer(modifier = Modifier.height(16.dp))

                EditField(
                    label = "生日",
                    value = birthday,
                    onValueChange = { birthday = it },
                    placeholder = "2002-06-15"
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditField(
                    label = "手机号",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "输入手机号",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditField(
                    label = "邮箱",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "输入邮箱",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFAFAFA)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("账号信息", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9E9E9E))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("用户ID", fontSize = 13.sp, color = Color(0xFFBDBDBD))
                            Text("$userId", fontSize = 13.sp, color = Color(0xFF757575))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFD0D0D0)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MintGreen,
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}

@Composable
private fun GenderChip(value: String, label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MintGreen else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFF9E9E9E)
        ),
        elevation = ButtonDefaults.buttonElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
