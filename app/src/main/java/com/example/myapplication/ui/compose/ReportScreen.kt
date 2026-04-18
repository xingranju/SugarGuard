package com.example.myapplication.ui.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SavedReport(
    val path: String,
    val period: String,
    val date: String,
    val ts: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var reports by remember { mutableStateOf(listOf<SavedReport>()) }
    var selectedReport by remember { mutableStateOf<SavedReport?>(null) }
    var showDeleteDialog by remember { mutableStateOf<SavedReport?>(null) }

    fun loadReports() {
        val prefs = context.getSharedPreferences("saved_reports", Context.MODE_PRIVATE)
        val json = prefs.getString("report_list", "[]") ?: "[]"
        val arr = try { JSONArray(json) } catch (_: Exception) { JSONArray() }
        val list = mutableListOf<SavedReport>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val file = File(obj.getString("path"))
            if (file.exists()) {
                list.add(SavedReport(
                    path = obj.getString("path"),
                    period = obj.optString("period", ""),
                    date = obj.optString("date", ""),
                    ts = obj.optLong("ts", 0)
                ))
            }
        }
        reports = list.sortedByDescending { it.ts }
    }

    fun deleteReport(report: SavedReport) {
        File(report.path).delete()
        val prefs = context.getSharedPreferences("saved_reports", Context.MODE_PRIVATE)
        val json = prefs.getString("report_list", "[]") ?: "[]"
        val arr = try { JSONArray(json) } catch (_: Exception) { JSONArray() }
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.optLong("ts", 0) != report.ts) {
                newArr.put(obj)
            }
        }
        prefs.edit().putString("report_list", newArr.toString()).apply()
        loadReports()
    }

    LaunchedEffect(Unit) { loadReports() }

    if (selectedReport != null) {
        ReportDetailView(
            report = selectedReport!!,
            onBack = { selectedReport = null },
            onShare = {
                try {
                    val file = File(selectedReport!!.path)
                    if (!file.exists()) {
                        android.widget.Toast.makeText(context, "报告文件不存在，可能已被清理", android.widget.Toast.LENGTH_SHORT).show()
                        return@ReportDetailView
                    }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "📊 我的糖知控糖分析报告（${selectedReport!!.period}）\n\n来「糖知」一起控糖吧！")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "分享报告截图"))
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "分享失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Text("报告历史", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Text("${reports.size}份报告", fontSize = 12.sp, color = Gray400)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "在「分析」页面点击「保存报告」按钮，长截图将保存到此处。",
            fontSize = 11.sp,
            color = Gray400,
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("暂无保存的报告", fontSize = 15.sp, color = Gray600)
                    Text("前往「分析」页面保存报告", fontSize = 12.sp, color = Gray400)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports, key = { it.ts }) { report ->
                    SavedReportCard(
                        report = report,
                        onClick = { selectedReport = report },
                        onDelete = { showDeleteDialog = report }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    showDeleteDialog?.let { report ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除报告", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除这份${report.period}报告吗？") },
            confirmButton = {
                Button(
                    onClick = { deleteReport(report); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消", color = Gray500) }
            }
        )
    }
}

@Composable
private fun SavedReportCard(
    report: SavedReport,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val periodColor = when (report.period) {
        "日" -> Color(0xFF42A5F5)
        "周" -> MintGreen
        "月" -> Color(0xFFAB47BC)
        "半年" -> Color(0xFFFF7043)
        else -> Gray400
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = File(report.path),
                contentDescription = "报告截图",
                modifier = Modifier.size(60.dp, 80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = periodColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${report.period}报告",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = periodColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(report.date, fontSize = 13.sp, color = Gray700)
                Spacer(modifier = Modifier.height(2.dp))
                Text("点击查看完整报告", fontSize = 11.sp, color = Gray400)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, "删除", tint = Color(0xFFE57373), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ReportDetailView(
    report: SavedReport,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Gray600)
            }
            Text("${report.period}报告详情", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onShare,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("分享", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            report.date,
            fontSize = 12.sp,
            color = Gray400,
            modifier = Modifier.padding(horizontal = 28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            AsyncImage(
                model = File(report.path),
                contentDescription = "报告长截图",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.FillWidth
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
