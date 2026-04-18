package com.example.myapplication.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.myapplication.model.DailyHealthRecord
import com.example.myapplication.model.HealthRecordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyHealthRecordScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var showHistory by remember { mutableStateOf(false) }
    if (showHistory) {
        HealthHistoryScreen(userId = userId, onBack = { showHistory = false })
        return
    }

    var waterCups by remember { mutableIntStateOf(0) }
    var exerciseMinutes by remember { mutableIntStateOf(0) }
    var sleepHours by remember { mutableFloatStateOf(0f) }
    var selectedMood by remember { mutableIntStateOf(2) }
    var isLoaded by remember { mutableStateOf(false) }
    var showExerciseCustom by remember { mutableStateOf(false) }
    var exerciseCustomText by remember { mutableStateOf("") }
    var showSleepCustom by remember { mutableStateOf(false) }
    var sleepCustomText by remember { mutableStateOf("") }

    val moods = listOf("😟" to "低落", "😐" to "一般", "😊" to "开心", "😁" to "很棒", "😴" to "疲惫")
    val exercisePresets = listOf(15, 30, 45, 60)
    val sleepPresets = listOf(5f, 6f, 7f, 8f, 9f)

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val record = withContext(Dispatchers.IO) {
                    val resp = RetrofitClient.getDailyHealthRecordApiService()
                        .getRecordByDate(todayStr).execute()
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) resp.body()?.data else null
                }
                if (record != null) {
                    waterCups = record.waterIntake?.div(250f)?.toInt() ?: 0
                    exerciseMinutes = record.exerciseMinutes?.toInt() ?: 0
                    sleepHours = record.sleepHours ?: 0f
                    selectedMood = when (record.mood) {
                        "low", "bad" -> 0; "normal" -> 1; "happy", "good" -> 2; "great", "excellent" -> 3; "tired", "terrible" -> 4; else -> 2
                    }
                }
                isLoaded = true
            } catch (_: Exception) { isLoaded = true }
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
            Text("每日健康数据", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Water intake
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("今日饮水量", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                        Text("建议 8 杯", fontSize = 12.sp, color = Gray400)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp).clickable { if (waterCups > 0) waterCups-- },
                            shape = CircleShape, color = Gray100
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray400)
                            }
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$waterCups", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                            Text("杯 (${waterCups * 250}ml)", fontSize = 12.sp, color = Gray400)
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Surface(
                            modifier = Modifier.size(48.dp).clickable { if (waterCups < 15) waterCups++ },
                            shape = CircleShape, color = MintGreen
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        (1..8).forEach { i ->
                            Surface(
                                modifier = Modifier.width(24.dp).height(32.dp).padding(horizontal = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = if (i <= waterCups) MintGreen else Gray100
                            ) {}
                        }
                    }
                }
            }

            // Exercise
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("今日运动", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("$exerciseMinutes", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray700)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("分钟", fontSize = 14.sp, color = Gray400)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        exercisePresets.forEach { mins ->
                            val isSelected = exerciseMinutes == mins && !showExerciseCustom
                            Surface(
                                modifier = Modifier.weight(1f).clickable {
                                    exerciseMinutes = mins
                                    showExerciseCustom = false
                                    exerciseCustomText = ""
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MintBg else Gray50,
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                            ) {
                                Text(
                                    "${mins}分钟", fontSize = 10.sp,
                                    color = if (isSelected) MintGreen else Gray600,
                                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        val isCustomSelected = showExerciseCustom
                        Surface(
                            modifier = Modifier.weight(1f).clickable {
                                showExerciseCustom = true
                                exerciseCustomText = if (exerciseMinutes > 0 && exerciseMinutes !in exercisePresets) exerciseMinutes.toString() else ""
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isCustomSelected) MintBg else Gray50,
                            border = if (isCustomSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                        ) {
                            Text(
                                "自定义", fontSize = 10.sp,
                                color = if (isCustomSelected) MintGreen else Gray600,
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (showExerciseCustom) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = exerciseCustomText,
                            onValueChange = { v ->
                                exerciseCustomText = v.filter { it.isDigit() }
                                exerciseCustomText.toIntOrNull()?.let { exerciseMinutes = it.coerceIn(0, 600) }
                            },
                            label = { Text("输入运动分钟数") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                        )
                    }
                }
            }

            // Sleep
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("昨晚睡眠", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        val displayHours = sleepHours.toInt()
                        val displayMins = ((sleepHours - displayHours) * 60).toInt()
                        Text("$displayHours", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray700)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("小时", fontSize = 14.sp, color = Gray400)
                        if (displayMins > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$displayMins", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray700)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("分钟", fontSize = 14.sp, color = Gray400)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sleepPresets.forEach { hrs ->
                            val isSelected = sleepHours == hrs && !showSleepCustom
                            val label = if (hrs >= 9) "9h+" else "${hrs.toInt()}h"
                            Surface(
                                modifier = Modifier.weight(1f).clickable {
                                    sleepHours = hrs
                                    showSleepCustom = false
                                    sleepCustomText = ""
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MintBg else Gray50,
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                            ) {
                                Text(label, fontSize = 10.sp, color = if (isSelected) MintGreen else Gray600,
                                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        }
                        val isCustomSelected = showSleepCustom
                        Surface(
                            modifier = Modifier.weight(1f).clickable {
                                showSleepCustom = true
                                sleepCustomText = if (sleepHours > 0 && sleepHours !in sleepPresets) sleepHours.toString() else ""
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isCustomSelected) MintBg else Gray50,
                            border = if (isCustomSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                        ) {
                            Text("自定义", fontSize = 10.sp, color = if (isCustomSelected) MintGreen else Gray600,
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                    }
                    if (showSleepCustom) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = sleepCustomText,
                            onValueChange = { v ->
                                sleepCustomText = v.filter { it.isDigit() || it == '.' }
                                sleepCustomText.toFloatOrNull()?.let { sleepHours = it.coerceIn(0f, 24f) }
                            },
                            label = { Text("输入睡眠小时数（如 7.5）") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                        )
                    }
                }
            }

            // Mood
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("今日心情", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        moods.forEachIndexed { index, (emoji, label) ->
                            val isSelected = selectedMood == index
                            Column(
                                modifier = Modifier
                                    .clickable { selectedMood = index }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MintGreen, RoundedCornerShape(12.dp)).padding(8.dp)
                                        else Modifier.padding(8.dp)
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(emoji, fontSize = 28.sp, modifier = Modifier.then(if (!isSelected) Modifier else Modifier))
                                Text(label, fontSize = 10.sp, color = if (isSelected) MintGreen else Gray400, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { showHistory = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen))
            ) {
                Text("查看历史记录", color = MintGreen, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val moodValues = listOf("bad", "normal", "good", "excellent", "terrible")
                            val request = HealthRecordRequest(
                                recordDate = today,
                                waterIntake = waterCups * 250f,
                                exerciseMinutes = exerciseMinutes.toFloat(),
                                sleepHours = sleepHours,
                                mood = moodValues.getOrElse(selectedMood) { "normal" }
                            )
                            val saveResp = withContext(Dispatchers.IO) {
                                RetrofitClient.getDailyHealthRecordApiService()
                                    .createOrUpdateRecord(request).execute()
                            }
                            if (saveResp.isSuccessful) {
                                Toast.makeText(context, "数据已保存", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("保存今日数据", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun HealthHistoryScreen(userId: Long, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var records by remember { mutableStateOf(listOf<DailyHealthRecord>()) }
    var editingRecord by remember { mutableStateOf<DailyHealthRecord?>(null) }
    var deleteTarget by remember { mutableStateOf<DailyHealthRecord?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.getDailyHealthRecordApiService().getRecentRecords(30).execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    records = resp.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {}
        }
    }

    if (editingRecord != null) {
        EditHealthRecordScreen(
            record = editingRecord!!,
            onBack = { editingRecord = null },
            onSaved = {
                editingRecord = null
                refreshTrigger++
            }
        )
        return
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("确认删除", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除 ${deleteTarget!!.recordDate} 的健康记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = deleteTarget!!.recordDate ?: return@TextButton
                        deleteTarget = null
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    RetrofitClient.getDailyHealthRecordApiService().deleteRecord(date).execute()
                                } catch (_: Exception) {}
                            }
                            refreshTrigger++
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回", tint = Gray600) }
            Spacer(modifier = Modifier.weight(1f))
            Text("健康记录历史", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无历史记录", color = Gray400)
            }
        } else {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                records.forEach { r ->
                    val moodEmoji = when (r.mood) {
                        "excellent" -> "😁"; "good" -> "😊"; "normal" -> "😐"; "bad" -> "😟"; "terrible" -> "😴"; else -> "😐"
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(r.recordDate ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(moodEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { editingRecord = r },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "编辑", tint = MintGreen, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { deleteTarget = r },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "删除", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("糖分", fontSize = 10.sp, color = Gray400)
                                    Text("${r.totalSugarIntake?.toInt() ?: 0}g", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                        color = if ((r.totalSugarIntake ?: 0f) > 25f) RedHigh else MintGreen)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("饮水", fontSize = 10.sp, color = Gray400)
                                    Text("${r.waterIntake?.toInt() ?: 0}ml", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("运动", fontSize = 10.sp, color = Gray400)
                                    Text("${r.exerciseMinutes?.toInt() ?: 0}min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("睡眠", fontSize = 10.sp, color = Gray400)
                                    Text("${r.sleepHours ?: 0}h", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EditHealthRecordScreen(
    record: DailyHealthRecord,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var waterCups by remember { mutableIntStateOf(record.waterIntake?.div(250f)?.toInt() ?: 0) }
    var exerciseMinutes by remember { mutableIntStateOf(record.exerciseMinutes?.toInt() ?: 0) }
    var sleepHours by remember { mutableFloatStateOf(record.sleepHours ?: 0f) }
    var selectedMood by remember {
        mutableIntStateOf(
            when (record.mood) {
                "bad" -> 0; "normal" -> 1; "good" -> 2; "excellent" -> 3; "terrible" -> 4; else -> 2
            }
        )
    }
    var showExerciseCustom by remember { mutableStateOf(exerciseMinutes !in listOf(15, 30, 45, 60) && exerciseMinutes > 0) }
    var exerciseCustomText by remember { mutableStateOf(if (showExerciseCustom) exerciseMinutes.toString() else "") }
    var showSleepCustom by remember { mutableStateOf(sleepHours !in listOf(5f, 6f, 7f, 8f, 9f) && sleepHours > 0f) }
    var sleepCustomText by remember { mutableStateOf(if (showSleepCustom) sleepHours.toString() else "") }
    var isSaving by remember { mutableStateOf(false) }

    val moods = listOf("😟" to "低落", "😐" to "一般", "😊" to "开心", "😁" to "很棒", "😴" to "疲惫")
    val exercisePresets = listOf(15, 30, 45, 60)
    val sleepPresets = listOf(5f, 6f, 7f, 8f, 9f)

    Column(
        modifier = Modifier.fillMaxSize().background(Gray50)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回", tint = Gray600) }
            Spacer(modifier = Modifier.weight(1f))
            Text("编辑 ${record.recordDate}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Water
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("饮水量", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                        Text("建议 8 杯", fontSize = 12.sp, color = Gray400)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(48.dp).clickable { if (waterCups > 0) waterCups-- }, shape = CircleShape, color = Gray100) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray400) }
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$waterCups", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                            Text("杯 (${waterCups * 250}ml)", fontSize = 12.sp, color = Gray400)
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Surface(modifier = Modifier.size(48.dp).clickable { if (waterCups < 15) waterCups++ }, shape = CircleShape, color = MintGreen) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                        }
                    }
                }
            }

            // Exercise
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("运动", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("$exerciseMinutes", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray700)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("分钟", fontSize = 14.sp, color = Gray400)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        exercisePresets.forEach { mins ->
                            val isSelected = exerciseMinutes == mins && !showExerciseCustom
                            Surface(
                                modifier = Modifier.weight(1f).clickable {
                                    exerciseMinutes = mins; showExerciseCustom = false; exerciseCustomText = ""
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MintBg else Gray50,
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                            ) {
                                Text("${mins}分钟", fontSize = 10.sp, color = if (isSelected) MintGreen else Gray600,
                                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f).clickable {
                                showExerciseCustom = true
                                exerciseCustomText = if (exerciseMinutes > 0 && exerciseMinutes !in exercisePresets) exerciseMinutes.toString() else ""
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (showExerciseCustom) MintBg else Gray50,
                            border = if (showExerciseCustom) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                        ) {
                            Text("自定义", fontSize = 10.sp, color = if (showExerciseCustom) MintGreen else Gray600,
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                    }
                    if (showExerciseCustom) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = exerciseCustomText,
                            onValueChange = { v ->
                                exerciseCustomText = v.filter { it.isDigit() }
                                exerciseCustomText.toIntOrNull()?.let { exerciseMinutes = it.coerceIn(0, 600) }
                            },
                            label = { Text("输入运动分钟数") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                        )
                    }
                }
            }

            // Sleep
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("睡眠", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        val dh = sleepHours.toInt()
                        val dm = ((sleepHours - dh) * 60).toInt()
                        Text("$dh", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray700)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("小时", fontSize = 14.sp, color = Gray400)
                        if (dm > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$dm", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray700)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("分钟", fontSize = 14.sp, color = Gray400)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sleepPresets.forEach { hrs ->
                            val isSelected = sleepHours == hrs && !showSleepCustom
                            val label = if (hrs >= 9) "9h+" else "${hrs.toInt()}h"
                            Surface(
                                modifier = Modifier.weight(1f).clickable {
                                    sleepHours = hrs; showSleepCustom = false; sleepCustomText = ""
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MintBg else Gray50,
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                            ) {
                                Text(label, fontSize = 10.sp, color = if (isSelected) MintGreen else Gray600,
                                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f).clickable {
                                showSleepCustom = true
                                sleepCustomText = if (sleepHours > 0 && sleepHours !in sleepPresets) sleepHours.toString() else ""
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (showSleepCustom) MintBg else Gray50,
                            border = if (showSleepCustom) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintGreen.copy(alpha = 0.3f))) else null
                        ) {
                            Text("自定义", fontSize = 10.sp, color = if (showSleepCustom) MintGreen else Gray600,
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                    }
                    if (showSleepCustom) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = sleepCustomText,
                            onValueChange = { v ->
                                sleepCustomText = v.filter { it.isDigit() || it == '.' }
                                sleepCustomText.toFloatOrNull()?.let { sleepHours = it.coerceIn(0f, 24f) }
                            },
                            label = { Text("输入睡眠小时数（如 7.5）") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, focusedLabelColor = MintGreen, cursorColor = MintGreen)
                        )
                    }
                }
            }

            // Mood
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("心情", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        moods.forEachIndexed { index, (emoji, label) ->
                            val isSelected = selectedMood == index
                            Column(
                                modifier = Modifier.clickable { selectedMood = index }
                                    .then(if (isSelected) Modifier.border(2.dp, MintGreen, RoundedCornerShape(12.dp)).padding(8.dp) else Modifier.padding(8.dp)),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(emoji, fontSize = 28.sp)
                                Text(label, fontSize = 10.sp, color = if (isSelected) MintGreen else Gray400, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Button(
                onClick = {
                    if (isSaving) return@Button
                    isSaving = true
                    scope.launch {
                        try {
                            val moodValues = listOf("bad", "normal", "good", "excellent", "terrible")
                            val request = HealthRecordRequest(
                                recordDate = record.recordDate ?: return@launch,
                                totalSugarIntake = record.totalSugarIntake,
                                totalCalories = record.totalCalories,
                                waterIntake = waterCups * 250f,
                                exerciseMinutes = exerciseMinutes.toFloat(),
                                sleepHours = sleepHours,
                                systolicBp = record.systolicBp,
                                diastolicBp = record.diastolicBp,
                                bloodGlucose = record.bloodGlucose,
                                weight = record.weight,
                                mood = moodValues.getOrElse(selectedMood) { "normal" }
                            )
                            withContext(Dispatchers.IO) {
                                RetrofitClient.getDailyHealthRecordApiService()
                                    .createOrUpdateRecord(request).execute()
                            }
                            onSaved()
                        } catch (_: Exception) {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("保存修改", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
