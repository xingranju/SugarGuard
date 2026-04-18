package com.example.myapplication.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.viewmodel.LocalAuthViewModel

val MintGreen = Color(0xFF26A69A)
val MintDark = Color(0xFF00897B)
val MintPressed = Color(0xFF1E8C82)
val MintLight = Color(0xFFB2DFDB)
val MintBg = Color(0xFFF0FDFA)
val Gray50 = Color(0xFFF9FAFB)
val Gray100 = Color(0xFFF3F4F6)
val Gray300 = Color(0xFFD1D5DB)
val Gray400 = Color(0xFF9CA3AF)
val Gray500 = Color(0xFF6B7280)
val Gray600 = Color(0xFF4B5563)
val Gray700 = Color(0xFF374151)
val Gray800 = Color(0xFF1F2937)
val RedHigh = Color(0xFFEF4444)
val OrangeMid = Color(0xFFF97316)

class ComposeLoginActivity : ComponentActivity() {
    private lateinit var authViewModel: LocalAuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this)[LocalAuthViewModel::class.java]

        if (authViewModel.isLoggedIn()) {
            startActivity(Intent(this, ComposeMainActivity::class.java))
            finish()
            return
        }

        setContent {
            SugarGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    var showLogin by remember { mutableStateOf(false) }

                    if (showLogin) {
                        LoginFormScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                startActivity(Intent(this, ComposeMainActivity::class.java))
                                finish()
                            },
                            onGoRegister = {
                                startActivity(Intent(this, ComposeRegisterActivity::class.java))
                            },
                            onBack = { showLogin = false }
                        )
                    } else {
                        SplashScreen(
                            onLogin = { showLogin = true },
                            onRegister = {
                                startActivity(Intent(this, ComposeRegisterActivity::class.java))
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::authViewModel.isInitialized && authViewModel.isLoggedIn()) {
            startActivity(Intent(this, ComposeMainActivity::class.java))
            finish()
        }
    }
}

@Composable
fun SplashScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .scale(pulseScale)
                .alpha(pulseAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = RoundedCornerShape(24.dp),
                color = MintGreen,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("\uD83C\uDF43", fontSize = 48.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "糖知",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MintGreen,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "轻松控糖，从了解开始",
                fontSize = 15.sp,
                color = Gray500,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureText(
                title = "拍照智能识糖",
                description = "毫秒级识别各类饮品与食物含糖量"
            )
            FeatureText(
                title = "专业控糖曲线",
                description = "基于2026最新营养学标准的健康建议"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                text = "注册新账号",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(MintGreen)
            )
        ) {
            Text(
                text = "已有账号？登录",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MintGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "当前版本 v3.5.1 | 2026年3月更新数据",
            fontSize = 10.sp,
            color = Gray300,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LoginFormScreen(
    authViewModel: LocalAuthViewModel,
    onLoginSuccess: () -> Unit,
    onGoRegister: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val loginResult by authViewModel.loginResult.observeAsState()
    val errorMessage by authViewModel.errorMessage.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)

    LaunchedEffect(loginResult) {
        if (loginResult != null) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(20.dp),
            color = MintGreen,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("\uD83C\uDF43", fontSize = 36.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "欢迎回来",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )

        Text(
            text = "登录您的糖知账号",
            fontSize = 14.sp,
            color = Gray400,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("用户名", color = Color(0xFFD0D0D0)) },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = Gray400)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MintGreen,
                focusedTextColor = Gray800,
                unfocusedTextColor = Gray800
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("密码", color = Color(0xFFD0D0D0)) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Gray400)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = Gray400
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MintGreen,
                focusedTextColor = Gray800,
                unfocusedTextColor = Gray800
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (username.isNotBlank() && password.isNotBlank()) {
                        authViewModel.login(username.trim(), password)
                    }
                }
            ),
            singleLine = true
        )

        if (!errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                color = RedHigh,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                authViewModel.login(username.trim(), password)
            },
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MintGreen,
                disabledContainerColor = MintLight
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "登 录",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("还没有账号？", fontSize = 14.sp, color = Gray400)
            Text(
                text = "立即注册",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MintGreen,
                modifier = Modifier
                    .clickable { onGoRegister() }
                    .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "返回",
            fontSize = 14.sp,
            color = Gray400,
            modifier = Modifier
                .clickable { onBack() }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureText(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        Text(
            text = description,
            fontSize = 12.sp,
            color = Gray400
        )
    }
}
