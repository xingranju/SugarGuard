package com.example.myapplication.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.AddMealRequest
import com.example.myapplication.model.HealthProfileRequest
import com.example.myapplication.model.HealthRecordRequest
import com.example.myapplication.model.UserDto
import com.example.myapplication.viewmodel.LocalAuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComposeRegisterActivity : ComponentActivity() {
    private lateinit var authViewModel: LocalAuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this)[LocalAuthViewModel::class.java]

        setContent {
            SugarGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    WizardScreen(
                        authViewModel = authViewModel,
                        onComplete = {
                            startActivity(
                                Intent(this, ComposeMainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                            finish()
                        },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardScreen(
    authViewModel: LocalAuthViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 6

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    var nickname by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("m") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sugarTarget by remember { mutableFloatStateOf(25f) }
    var selectedActivity by remember { mutableIntStateOf(0) }
    var healthConditions by remember { mutableStateOf(setOf(0)) }

    var registerError by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    val registerResult: UserDto? by authViewModel.registerResult.observeAsState()
    val errorMessage by authViewModel.errorMessage.observeAsState()

    LaunchedEffect(registerResult) {
        if (registerResult != null && currentStep == 0) {
            isRegistering = false
            registerError = ""
            currentStep = 1
            nickname = regUsername
        }
    }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrEmpty() && currentStep == 0 && isRegistering) {
            registerError = errorMessage!!
            isRegistering = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 48.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        if (currentStep > 0) currentStep-- else onBack()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = if (currentStep == 0) "创建账号" else "您的控糖档案",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )

                Text(
                    text = "${currentStep + 1} / $totalSteps",
                    fontSize = 13.sp,
                    color = Color(0xFFBDBDBD)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / totalSteps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MintGreen,
                trackColor = Color(0xFFF5F5F5),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> WizardStepRegister(
                        username = regUsername,
                        onUsernameChange = { regUsername = it },
                        email = regEmail,
                        onEmailChange = { regEmail = it },
                        password = regPassword,
                        onPasswordChange = { regPassword = it },
                        confirmPassword = regConfirmPassword,
                        onConfirmPasswordChange = { regConfirmPassword = it },
                        errorMessage = registerError
                    )
                    1 -> WizardStep1(nickname, { nickname = it }, selectedGender, { selectedGender = it }, age, { age = it })
                    2 -> WizardStep2(height, { height = it }, weight, { weight = it })
                    3 -> WizardStep3(sugarTarget, { sugarTarget = it })
                    4 -> WizardStep4(selectedActivity, { selectedActivity = it })
                    5 -> WizardStep5(healthConditions, { healthConditions = it })
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        currentStep--
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFFBDBDBD)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("上一步", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    when {
                        currentStep == 0 -> {
                            registerError = ""
                            when {
                                regUsername.isBlank() -> registerError = "请输入用户名"
                                regUsername.length < 2 -> registerError = "用户名至少2个字符"
                                regPassword.isBlank() -> registerError = "请输入密码"
                                regPassword.length < 6 -> registerError = "密码至少6位"
                                regPassword != regConfirmPassword -> registerError = "两次密码不一致"
                                else -> {
                                    isRegistering = true
                                    authViewModel.register(
                                        username = regUsername.trim(),
                                        email = regEmail.trim().ifBlank { "${regUsername.trim()}@sugarguard.local" },
                                        password = regPassword,
                                        confirmPassword = regConfirmPassword,
                                        phone = null, gender = null, birthday = null
                                    )
                                }
                            }
                        }
                        currentStep < totalSteps - 1 -> currentStep++
                        else -> {
                            scope.launch {
                                try {
                                    val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                                    if (nickname.isNotBlank()) {
                                        prefs.edit().putString("username", nickname).apply()
                                    }
                                    val userIdLong = prefs.getLong("user_id", registerResult?.id ?: 1L)
                                    val userId = userIdLong.toInt()
                                    val today = java.time.LocalDate.now().toString()
                                    val activityLevels = listOf("sedentary", "light", "moderate", "active")
                                    val conditionLabels = listOf("healthy", "diabetes_type2", "nut_lactose_sensitive", "keto_diet")
                                    val selectedConditions = healthConditions.mapNotNull { conditionLabels.getOrNull(it) }

                                    withContext(Dispatchers.IO) {
                                        try {
                                            val profileApi = RetrofitClient.getUserProfileApiService()
                                            val profileRequest = HealthProfileRequest(
                                                age = age.toIntOrNull() ?: 20,
                                                gender = if (selectedGender == "m") "male" else "female",
                                                height = height.toFloatOrNull() ?: 170f,
                                                weight = weight.toFloatOrNull() ?: 60f,
                                                healthConditions = selectedConditions.joinToString(","),
                                                activityLevel = activityLevels.getOrElse(selectedActivity) { "moderate" },
                                                sugarLimit = sugarTarget,
                                                calorieLimit = 2000f,
                                                waterGoal = 2000f
                                            )
                                            profileApi.createOrUpdateHealthProfile(profileRequest).execute()

                                            val mealApi = RetrofitClient.getMealApiService()
                                            val sampleMeals = listOf(
                                                Triple("07:30:00", "breakfast", Triple("无糖酸奶 + 全麦面包", 6.0, 230.0)),
                                                Triple("12:15:00", "lunch", Triple("糖醋排骨盖饭", 25.0, 520.0)),
                                                Triple("15:30:00", "snack", Triple("杨枝甘露（全糖）", 38.0, 350.0))
                                            )
                                            var totalSugar = 0.0
                                            var totalCal = 0.0
                                            for ((time, type, data) in sampleMeals) {
                                                val (name, sugar, cal) = data
                                                totalSugar += sugar
                                                totalCal += cal
                                                val mealRequest = AddMealRequest(
                                                    userId = userId,
                                                    mealDate = today,
                                                    mealTime = time,
                                                    mealType = type,
                                                    foodName = name,
                                                    sugarContent = sugar,
                                                    calories = cal
                                                )
                                                mealApi.addMeal(mealRequest).execute()
                                            }

                                            val healthApi = RetrofitClient.getDailyHealthRecordApiService()
                                            val recordRequest = HealthRecordRequest(
                                                recordDate = today,
                                                totalSugarIntake = totalSugar.toFloat(),
                                                totalCalories = totalCal.toFloat(),
                                                waterIntake = 1500f,
                                                exerciseMinutes = 30f,
                                                sleepHours = 7f,
                                                mood = "good",
                                                notes = "注册向导初始数据"
                                            )
                                            healthApi.createOrUpdateRecord(recordRequest).execute()
                                        } catch (_: Exception) {
                                        }
                                    }
                                } catch (_: Exception) {
                                }
                                onComplete()
                            }
                        }
                    }
                },
                enabled = !isRegistering,
                modifier = Modifier
                    .weight(if (currentStep > 0) 2f else 1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintGreen,
                    disabledContainerColor = MintLight
                ),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when (currentStep) {
                            0 -> "注册并继续"
                            totalSteps - 1 -> "完成并进入首页"
                            else -> "下一步"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardStepRegister(
    username: String, onUsernameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    errorMessage: String
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    Column {
        Text("创建您的账号", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text("注册后即可开始您的健康之旅", fontSize = 14.sp, color = Color(0xFFBDBDBD), modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        WizardLabel("用户名 *")
        TextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = { Text("输入用户名", color = Color(0xFFD0D0D0)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Gray400) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MintGreen,
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        WizardLabel("邮箱（选填）")
        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("输入邮箱地址", color = Color(0xFFD0D0D0)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Gray400) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MintGreen,
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        WizardLabel("密码 *")
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("至少6位密码", color = Color(0xFFD0D0D0)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gray400) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null, tint = Gray400
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
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        WizardLabel("确认密码 *")
        TextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = { Text("再次输入密码", color = Color(0xFFD0D0D0)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gray400) },
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null, tint = Gray400
                    )
                }
            },
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MintGreen,
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = RedHigh,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WizardStep1(
    nickname: String, onNicknameChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit
) {
    Column {
        Text("如何称呼您？", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text("我们将为您定制专属的糖分预算", fontSize = 14.sp, color = Color(0xFFBDBDBD), modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        WizardLabel("您的昵称")
        WizardTextField(value = nickname, onValueChange = onNicknameChange, placeholder = "输入称呼")

        Spacer(modifier = Modifier.height(24.dp))

        WizardLabel("性别")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderButton("男", gender == "m", Modifier.weight(1f)) { onGenderChange("m") }
            GenderButton("女", gender == "f", Modifier.weight(1f)) { onGenderChange("f") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        WizardLabel("年龄")
        WizardTextField(value = age, onValueChange = onAgeChange, placeholder = "25", keyboardType = KeyboardType.Number)
    }
}

@Composable
private fun WizardStep2(
    height: String, onHeightChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit
) {
    Column {
        Text("身体指标数据", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text("精准的BMI计算需要这些数据", fontSize = 14.sp, color = Color(0xFFBDBDBD), modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        WizardLabel("身高 (cm)")
        WizardTextField(value = height, onValueChange = onHeightChange, placeholder = "175", keyboardType = KeyboardType.Number)

        Spacer(modifier = Modifier.height(24.dp))

        WizardLabel("体重 (kg)")
        WizardTextField(value = weight, onValueChange = onWeightChange, placeholder = "65.0", keyboardType = KeyboardType.Decimal)
    }
}

@Composable
private fun WizardStep3(sugarTarget: Float, onSugarTargetChange: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("每日糖分目标", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Row {
            Text("推荐控糖目标：", fontSize = 14.sp, color = Color(0xFFBDBDBD))
            Text("25g", fontSize = 14.sp, color = MintGreen, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(64.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${sugarTarget.toInt()}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = MintGreen
            )
            Text("g", fontSize = 18.sp, color = MintGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }

        Text(
            text = "相当于约 ${String.format("%.1f", sugarTarget / 30f)} 碗大米饭的碳水量",
            fontSize = 12.sp,
            color = Color(0xFFBDBDBD),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Slider(
            value = sugarTarget,
            onValueChange = onSugarTargetChange,
            valueRange = 15f..50f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MintGreen,
                activeTrackColor = MintGreen,
                inactiveTrackColor = Color(0xFFE0E0E0)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("严格 (15g)", fontSize = 10.sp, color = Color(0xFFBDBDBD))
            Text("中等 (25g)", fontSize = 10.sp, color = Color(0xFFBDBDBD))
            Text("宽松 (50g)", fontSize = 10.sp, color = Color(0xFFBDBDBD))
        }
    }
}

@Composable
private fun WizardStep4(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val activities = listOf(
        Triple("\uD83D\uDCBB", "久坐办公", "缺乏规律运动，基础消耗低"),
        Triple("\uD83D\uDEB6", "轻度活跃", "日常通勤走动，每周运动1-2次"),
        Triple("\uD83D\uDEB4", "中度活跃", "坚持健身，每周中等强度运动3次以上"),
        Triple("\uD83D\uDD25", "重度活跃", "高强度运动/体力劳动，每日大量消耗")
    )

    Column {
        Text("日常活动水平", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text("选择最符合您现状的一项", fontSize = 14.sp, color = Color(0xFFBDBDBD), modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        activities.forEachIndexed { index, (icon, title, desc) ->
            val isSelected = selectedIndex == index
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onSelect(index) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MintGreen else Color(0xFFF0F0F0)
                ),
                color = if (isSelected) MintBg else Color(0xFFFAFAFA)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(icon, fontSize = 24.sp)
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Text(desc, fontSize = 11.sp, color = Color(0xFFBDBDBD))
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStep5(selected: Set<Int>, onSelect: (Set<Int>) -> Unit) {
    val conditions = listOf(
        "我身体很健康，无基础病",
        "我患有 2型糖尿病",
        "我对 坚果/乳糖 敏感",
        "正在进行 减脂/生酮 计划"
    )

    Column {
        Text("健康状况申报", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text("勾选符合您的选项，我们将智能避雷", fontSize = 14.sp, color = Color(0xFFBDBDBD), modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        conditions.forEachIndexed { index, label ->
            val isChecked = selected.contains(index)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable {
                        onSelect(
                            if (isChecked) selected - index else selected + index
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFAFAFA)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            onSelect(
                                if (isChecked) selected - index else selected + index
                            )
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MintGreen,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(label, fontSize = 14.sp, color = Color(0xFF555555))
                }
            }
        }
    }
}

@Composable
private fun WizardLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF9E9E9E),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFFD0D0D0)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedContainerColor = Color(0xFFFAFAFA),
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

@Composable
private fun GenderButton(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MintGreen else Color(0xFFFAFAFA),
            contentColor = if (isSelected) Color.White else Color(0xFF9E9E9E)
        ),
        border = if (!isSelected) BorderStroke(1.dp, Color.Transparent) else null,
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
