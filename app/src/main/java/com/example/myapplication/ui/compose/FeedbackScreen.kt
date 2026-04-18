package com.example.myapplication.ui.compose

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val feedbackTypes = listOf("功能建议", "Bug反馈", "数据问题", "其他")
    val relatedPages = listOf("首页", "拍照识别", "识别结果", "饮食日记", "健康分析", "AI助手")

    var selectedType by remember { mutableIntStateOf(0) }
    var description by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var selectedPages by remember { mutableStateOf(setOf<Int>()) }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> screenshotUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Text("反馈意见", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Feedback type
                    Column {
                        Text("反馈类型", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gray600)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            feedbackTypes.forEachIndexed { index, label ->
                                val isSelected = selectedType == index
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedType = index },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MintGreen else Gray50
                                ) {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else Gray600,
                                        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Description
                    Column {
                        Text("详细描述", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gray600)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("请描述你遇到的问题或建议...", fontSize = 14.sp, color = Gray300) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Gray50,
                                unfocusedContainerColor = Gray50,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MintGreen
                            )
                        )
                    }

                    // Related pages
                    Column {
                        Text("相关页面（可选）", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gray600)
                        Spacer(modifier = Modifier.height(8.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            relatedPages.forEachIndexed { index, label ->
                                val isSelected = selectedPages.contains(index)
                                Surface(
                                    modifier = Modifier.clickable {
                                        selectedPages = if (isSelected) selectedPages - index else selectedPages + index
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) MintGreen else Gray50
                                ) {
                                    Text(
                                        label, fontSize = 12.sp,
                                        color = if (isSelected) Color.White else Gray600,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Screenshot upload area
                    Column {
                        Text("截图（可选）", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gray600)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent
                        ) {
                            if (screenshotUri != null) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = screenshotUri,
                                        contentDescription = "截图",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                                        shape = RoundedCornerShape(12.dp), color = MintGreen
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp).padding(2.dp))
                                    }
                                }
                            } else
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Image, null, tint = Gray300, modifier = Modifier.size(28.dp))
                                Text("点击添加截图", fontSize = 12.sp, color = Gray400)
                            }
                        }
                    }

                    // Contact info
                    Column {
                        Text("联系方式（可选）", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gray600)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = contactInfo,
                            onValueChange = { contactInfo = it },
                            placeholder = { Text("邮箱或手机号，方便我们回复", fontSize = 14.sp, color = Gray300) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
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
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Submit button
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Button(
                onClick = {
                    if (description.isBlank()) {
                        Toast.makeText(context, "请填写反馈内容", Toast.LENGTH_SHORT).show()
                    } else {
                        val feedbackPrefs = context.getSharedPreferences("feedback_data", Context.MODE_PRIVATE)
                        val feedbackCount = feedbackPrefs.getInt("count", 0) + 1
                        feedbackPrefs.edit()
                            .putInt("count", feedbackCount)
                            .putString("feedback_${feedbackCount}_type", feedbackTypes[selectedType])
                            .putString("feedback_${feedbackCount}_desc", description)
                            .putString("feedback_${feedbackCount}_contact", contactInfo)
                            .putLong("feedback_${feedbackCount}_time", System.currentTimeMillis())
                            .apply()
                        Toast.makeText(context, "反馈已提交，感谢您的建议！", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("提交反馈", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
