package com.example.myapplication.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpScreen(onBack: () -> Unit) {
    var showFeedback by remember { mutableStateOf(false) }

    if (showFeedback) {
        FeedbackScreen(onBack = { showFeedback = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Text("帮助与反馈", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FAQ Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Gray50,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ) {
                        Text(
                            "常见问题",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray600,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    HelpFaqItem("如何拍照识别食物含糖量？", "打开首页，点击底部导航栏中间的扫描按钮。将食物或饮品放在取景框中央，点击拍照按钮即可自动识别。")
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray100)
                    HelpFaqItem("AI识别结果不准确怎么办？", "在识别结果页面点击「修正结果」按钮，可以手动修改食物名称和含糖量。你的反馈也会帮助我们改进AI模型。")
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray100)
                    HelpFaqItem("如何修改每日控糖目标？", "进入「我的」→「健康档案」，拖动糖分目标滑块调整每日目标值（15g-50g）。")
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray100)
                    HelpFaqItem("数据会同步到云端吗？", "所有数据默认加密存储在本地设备。你可以在「设置」→「隐私与安全」中开启云端同步。")
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray100)
                    HelpFaqItem("\"方糖\"可视化是什么意思？", "方糖可视化将食物的含糖量转换为方糖数量展示（1块方糖≈4g糖），帮助你更直观地理解糖分含量。")
                }
            }

            // Contact section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Gray50,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ) {
                        Text(
                            "联系我们",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray600,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Email, null, tint = MintGreen, modifier = Modifier.size(20.dp))
                            Text("邮箱", fontSize = 14.sp, color = Gray800)
                        }
                        Text("support@tangzhi.app", fontSize = 14.sp, color = Gray400)
                    }
                }
            }

            // Feedback button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("提交反馈意见", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray700)
                    Text("你的反馈对我们非常重要", fontSize = 12.sp, color = Gray400, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showFeedback = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("写反馈意见", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Version info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("糖知 v3.5.1", fontSize = 10.sp, color = Gray300)
                Text("© 2026 糖知团队", fontSize = 10.sp, color = Gray300)
            }
        }
    }
}

@Composable
private fun HelpFaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Gray800,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = Gray400,
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                answer,
                fontSize = 12.sp,
                color = Gray600,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
