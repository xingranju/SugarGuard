package com.example.myapplication.ui.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.api.RankingItem
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val RankingHeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF26A69A), Color(0xFF1B8A7D), Color(0xFF15796E))
)
private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD7F32)

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
                title = { Text("社区排行", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F7FA)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MintGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "榜",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无排行数据", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("快去打卡成为第一名吧", fontSize = 14.sp, color = Color(0xFFBDBDBD))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F7FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val top3 = rankings.take(3)
                if (top3.isNotEmpty()) {
                    item {
                        RankingHeaderCard(top3, userId)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                item {
                    Text(
                        "排行榜",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                itemsIndexed(rankings) { _, item ->
                    RankingListItem(item = item, isCurrentUser = item.userId == userId)
                }
            }
        }
    }
}

@Composable
private fun RankingHeaderCard(top3: List<RankingItem>, currentUserId: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RankingHeaderGradient)
                .padding(horizontal = 16.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "控糖排行榜",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "坚持打卡，一起变得更健康",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (top3.size >= 2) {
                        TopRankItem(
                            item = top3[1],
                            rank = 2,
                            medalColor = SilverColor,
                            avatarSize = 56,
                            isCurrentUser = top3[1].userId == currentUserId
                        )
                    }
                    if (top3.isNotEmpty()) {
                        TopRankItem(
                            item = top3[0],
                            rank = 1,
                            medalColor = GoldColor,
                            avatarSize = 72,
                            isCurrentUser = top3[0].userId == currentUserId
                        )
                    }
                    if (top3.size >= 3) {
                        TopRankItem(
                            item = top3[2],
                            rank = 3,
                            medalColor = BronzeColor,
                            avatarSize = 52,
                            isCurrentUser = top3[2].userId == currentUserId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarImage(
    avatarUrl: String?,
    username: String,
    size: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    bgColor: Color = Color.White.copy(alpha = 0.2f)
) {
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = username,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.take(1).uppercase(),
                fontSize = (size / 3).sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun TopRankItem(
    item: RankingItem,
    rank: Int,
    medalColor: Color,
    avatarSize: Int,
    isCurrentUser: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width((avatarSize + 28).dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(avatarSize.dp)
                    .then(
                        if (rank == 1) Modifier.shadow(8.dp, CircleShape, ambientColor = medalColor)
                        else Modifier
                    )
                    .border(
                        width = if (rank == 1) 3.dp else 2.dp,
                        color = medalColor.copy(alpha = 0.8f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AvatarImage(
                    avatarUrl = item.avatarUrl,
                    username = item.username,
                    size = avatarSize
                )
            }
            Box(
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(medalColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            item.username,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentUser) Color.White else Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            "连续${item.streak}天",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.65f)
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = medalColor.copy(alpha = 0.3f),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                "${item.badgeCount}枚徽章",
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun RankingListItem(item: RankingItem, isCurrentUser: Boolean) {
    val rankColors = mapOf(
        1 to GoldColor,
        2 to SilverColor,
        3 to BronzeColor
    )
    val rankColor = rankColors[item.rank]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) MintGreen.copy(alpha = 0.06f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (rankColor != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(rankColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${item.rank}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    "${item.rank}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) MintGreen else Color(0xFF999999),
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            AvatarImage(
                avatarUrl = item.avatarUrl,
                username = item.username,
                size = 44,
                textColor = if (isCurrentUser) MintGreen else Color(0xFF888888),
                bgColor = if (isCurrentUser) MintGreen.copy(alpha = 0.15f) else Color(0xFFF0F0F0)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isCurrentUser) MintGreen else Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "连续${item.streak}天 · 共${item.totalCheckIns}次打卡",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF8E1)
            ) {
                Text(
                    "${item.badgeCount}徽章",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF8F00),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
