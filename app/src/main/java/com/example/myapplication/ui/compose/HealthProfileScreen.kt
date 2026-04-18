package com.example.myapplication.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.HealthProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HealthProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var height by remember { mutableStateOf("165") }
    var weight by remember { mutableStateOf("52") }
    var age by remember { mutableStateOf("20") }
    var gender by remember { mutableStateOf("f") }
    var selectedActivity by remember { mutableIntStateOf(0) }
    var sugarGoal by remember { mutableFloatStateOf(25f) }
    val tastePref = context.getSharedPreferences("health_prefs", Context.MODE_PRIVATE)
        .getString("taste_preference", "微甜") ?: "微甜"
    var selectedTaste by remember { mutableIntStateOf(listOf("甜", "微甜", "不甜").indexOf(tastePref).coerceAtLeast(0)) }
    var healthConditions by remember { mutableStateOf(setOf(0)) }

    data class Activity(val key: String, val label: String, val desc: String)
    val activities = listOf(
        Activity("sedentary", "久坐", "办公室为主"),
        Activity("light", "轻度", "偶尔运动"),
        Activity("moderate", "中度", "定期健身"),
        Activity("active", "重度", "高强度运动")
    )
    val tastes = listOf("甜", "微甜", "不甜")
    val conditions = listOf("无特殊情况", "糖尿病", "食物过敏")

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val profile = withContext(Dispatchers.IO) {
                    val resp = RetrofitClient.getUserProfileApiService().getHealthProfile().execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
                }
                profile?.let {
                    height = it.height.toInt().toString()
                    weight = it.weight.toInt().toString()
                    age = it.age.toString()
                    gender = it.gender
                    sugarGoal = it.sugarLimit ?: 25f
                    selectedActivity = when (it.activityLevel) {
                        "久坐", "sedentary" -> 0; "轻度", "light" -> 1
                        "中度", "moderate" -> 2; "重度", "active" -> 3; else -> 0
                    }
                    val condStr = it.healthConditions ?: ""
                    healthConditions = mutableSetOf<Int>().apply {
                        if (condStr.contains("healthy") || condStr.contains("无")) add(0)
                        if (condStr.contains("diabetes") || condStr.contains("糖尿")) add(1)
                        if (condStr.contains("allergy") || condStr.contains("过敏")) add(2)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("健康档案", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Body data
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("身体数据", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileField("身高 (cm)", height, { height = it }, KeyboardType.Number, Modifier.weight(1f))
                        ProfileField("体重 (kg)", weight, { weight = it }, KeyboardType.Number, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileField("年龄", age, { age = it }, KeyboardType.Number, Modifier.weight(1f))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("性别", fontSize = 12.sp, color = Gray600)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("male" to "男", "female" to "女").forEach { (value, label) ->
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { gender = value },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (gender == value) MintGreen else Gray50
                                    ) {
                                        Text(
                                            label, fontSize = 12.sp,
                                            color = if (gender == value) Color.White else Gray600,
                                            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Activity level
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("活动水平", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activities.forEachIndexed { index, act ->
                            val isSelected = selectedActivity == index
                            Surface(
                                modifier = Modifier.weight(1f).clickable { selectedActivity = index },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MintBg else Gray50,
                                border = if (isSelected) BorderStroke(2.dp, MintGreen) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(act.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                                    Text(act.desc, fontSize = 10.sp, color = Gray400)
                                }
                            }
                        }
                    }
                }
            }

            // Health conditions
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("健康状况", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(12.dp))
                    conditions.forEachIndexed { index, label ->
                        val isChecked = healthConditions.contains(index)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    healthConditions = if (isChecked) healthConditions - index else healthConditions + index
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Gray50
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        healthConditions = if (isChecked) healthConditions - index else healthConditions + index
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = MintGreen, checkmarkColor = Color.White)
                                )
                                Text(label, fontSize = 14.sp, color = Gray700)
                            }
                        }
                    }
                }
            }

            // Sugar goal
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("每日控糖目标", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${sugarGoal.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        Text("g / 天", fontSize = 14.sp, color = Gray400, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = sugarGoal,
                        onValueChange = { sugarGoal = it },
                        valueRange = 15f..50f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = MintGreen, activeTrackColor = MintGreen, inactiveTrackColor = Color(0xFFE0E0E0))
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("严格 15g", fontSize = 10.sp, color = Gray400)
                        Text("宽松 50g", fontSize = 10.sp, color = Gray400)
                    }
                }
            }

            // Taste preference
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("口味偏好", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tastes.forEachIndexed { index, label ->
                            Surface(
                                modifier = Modifier.weight(1f).clickable { selectedTaste = index },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedTaste == index) MintGreen else Gray50
                            ) {
                                Text(
                                    label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                    color = if (selectedTaste == index) Color.White else Gray600,
                                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Text("口味偏好将影响 AI 推荐的饮品选项", fontSize = 10.sp, color = Gray400, modifier = Modifier.padding(top = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Save button
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val conditionLabels = listOf("healthy", "diabetes_type2", "allergy")
                            val selectedCondStr = healthConditions.mapNotNull { conditionLabels.getOrNull(it) }.joinToString(",")
                            val request = HealthProfileRequest(
                                age = age.toIntOrNull() ?: 20,
                                gender = gender,
                                height = height.toFloatOrNull() ?: 165f,
                                weight = weight.toFloatOrNull() ?: 52f,
                                activityLevel = activities[selectedActivity].key,
                                sugarLimit = sugarGoal,
                                healthConditions = selectedCondStr
                            )
                            val saveResp = withContext(Dispatchers.IO) {
                                RetrofitClient.getUserProfileApiService()
                                    .createOrUpdateHealthProfile(request).execute()
                            }
                            if (saveResp.isSuccessful && saveResp.body()?.isSuccess == true) {
                                val tastes = listOf("甜", "微甜", "不甜")
                                context.getSharedPreferences("health_prefs", Context.MODE_PRIVATE)
                                    .edit().putString("taste_preference", tastes.getOrNull(selectedTaste) ?: "微甜").apply()
                                Toast.makeText(context, "档案已保存", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "保存失败: ${saveResp.body()?.message ?: saveResp.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("保存更新", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String, value: String, onValueChange: (String) -> Unit,
    keyboardType: KeyboardType, modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = Gray600)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Gray50,
                unfocusedContainerColor = Gray50,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MintGreen
            )
        )
    }
}
