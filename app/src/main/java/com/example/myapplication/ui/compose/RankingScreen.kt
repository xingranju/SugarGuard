package com.example.myapplication.ui.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.RankingItem
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", 1)

    var rankings by remember { mutableStateOf<List<RankingItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.getCheckInApiService().getRanking(20).execute()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    rankings = resp.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("社区排行") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else if (rankings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("暂无排行数据", fontSize = 16.sp, color = Color.Gray)
                    Text("快去打卡成为第一名吧", fontSize = 14.sp, color = Color(0xFFBDBDBD))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F7FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val top3 = rankings.take(3)
                if (top3.isNotEmpty()) {
                    item {
                        TopThreeSection(top3, userId)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                val rest = rankings.drop(3)
                itemsIndexed(rest) { _, item ->
                    RankingListItem(item = item, isCurrentUser = item.userId == userId)
                }
            }
        }
    }
}

@Composable
private fun TopThreeSection(top3: List<RankingItem>, currentUserId: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            if (top3.size >= 2) {
                TopRankItem(item = top3[1], medalColor = Color(0xFFC0C0C0), size = 60, isCurrentUser = top3[1].userId == currentUserId)
            }
            if (top3.isNotEmpty()) {
                TopRankItem(item = top3[0], medalColor = Color(0xFFFFD700), size = 76, isCurrentUser = top3[0].userId == currentUserId)
            }
            if (top3.size >= 3) {
                TopRankItem(item = top3[2], medalColor = Color(0xFFCD7F32), size = 56, isCurrentUser = top3[2].userId == currentUserId)
            }
        }
    }
}

@Composable
private fun TopRankItem(item: RankingItem, medalColor: Color, size: Int, isCurrentUser: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width((size + 24).dp)) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier.size(size.dp).clip(CircleShape)
                    .background(if (isCurrentUser) MintGreen.copy(alpha = 0.2f) else Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.username.take(1).uppercase(),
                    fontSize = (size / 3).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) MintGreen else Color.Gray
                )
            }
            Box(
                modifier = Modifier.offset(y = (-8).dp).size(24.dp).clip(CircleShape).background(medalColor),
                contentAlignment = Alignment.Center
            ) {
                Text("${item.rank}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(item.username, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = if (isCurrentUser) MintGreen else Color.Black, maxLines = 1, textAlign = TextAlign.Center)
        Text("连续${item.streak}天", fontSize = 11.sp, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
            Text("${item.badgeCount}", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun RankingListItem(item: RankingItem, isCurrentUser: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCurrentUser) MintGreen.copy(alpha = 0.08f) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${item.rank}", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = Color.Gray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)

            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.username.take(1).uppercase(), fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) MintGreen else Color.Gray)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.username, fontWeight = FontWeight.Medium,
                    color = if (isCurrentUser) MintGreen else Color.Black)
                Text("连续${item.streak}天 | 共${item.totalCheckIns}次", fontSize = 12.sp, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Text("${item.badgeCount}", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
            }
        }
    }
}
