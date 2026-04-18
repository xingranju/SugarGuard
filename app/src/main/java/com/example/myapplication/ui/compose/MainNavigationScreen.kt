package com.example.myapplication.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.AIServiceViewModel
import com.example.myapplication.viewmodel.LocalMealViewModel

@Composable
fun MainNavigationScreen(
    onBack: () -> Unit,
    notificationLaunch: NotificationLaunchInfo? = null,
    onNotificationLaunchConsumed: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val viewModel: AIServiceViewModel = viewModel()
    val mealViewModel: LocalMealViewModel = viewModel()
    val context = LocalContext.current
    val userIdLong = remember {
        context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
            .getLong("user_id", 1)
    }

    var showSearch by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    // 当用户从系统通知栏进入时高亮/标记已读的通知 id
    var notifHighlightId by remember { mutableStateOf<Long?>(null) }

    // 处理系统通知栏点击：打开通知列表或直接跳转目标页
    LaunchedEffect(notificationLaunch) {
        val info = notificationLaunch ?: return@LaunchedEffect
        notifHighlightId = info.notificationId
        val target = info.targetPage
        if (target.isNullOrBlank()) {
            showNotifications = true
        } else {
            when (target) {
                "diary" -> { selectedTab = 1; showNotifications = false }
                "analysis" -> { selectedTab = 3; showNotifications = false }
                "health_record" -> { selectedTab = 4; showNotifications = false }
                "recognition" -> { selectedTab = 2; showNotifications = false }
                "chat" -> { showChat = true; showNotifications = false }
                "profile" -> { selectedTab = 4; showNotifications = false }
                else -> { showNotifications = true }
            }
            val id = info.notificationId
            if (id != null && id > 0) {
                markNotificationAsRead(id, userIdLong)
            }
        }
        onNotificationLaunchConsumed()
    }

    if (showSearch) {
        SearchScreen(onBack = { showSearch = false })
        return
    }
    if (showChat) {
        ChatScreen(
            viewModel = viewModel,
            onBack = { showChat = false }
        )
        return
    }
    if (showNotifications) {
        NotificationsScreen(
            onBack = {
                showNotifications = false
                notifHighlightId = null
            },
            onNavigateTo = { targetPage ->
                showNotifications = false
                notifHighlightId = null
                when (targetPage) {
                    "diary" -> selectedTab = 1
                    "analysis" -> selectedTab = 3
                    "health_record" -> selectedTab = 4
                    "recognition" -> selectedTab = 2
                    "chat" -> showChat = true
                    "profile" -> selectedTab = 4
                    else -> {}
                }
            },
            initialHighlightId = notifHighlightId
        )
        return
    }
    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    val hideBottomBar = selectedTab == 2

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (!hideBottomBar) Modifier.padding(bottom = 80.dp) else Modifier)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onNavigateToRecognition = { selectedTab = 2 },
                    onNavigateToDiary = { selectedTab = 1 },
                    onNavigateToChat = { showChat = true },
                    onNavigateToProfile = { selectedTab = 4 },
                    onNavigateToNotifications = { showNotifications = true }
                )
                1 -> DiaryScreen(
                    viewModel = viewModel,
                    mealViewModel = mealViewModel,
                    onNavigateToCamera = { selectedTab = 2 },
                    onNavigateToSearch = { showSearch = true },
                    onNavigateToChat = { showChat = true }
                )
                2 -> RecognitionScreen(
                    viewModel = viewModel,
                    mealViewModel = mealViewModel,
                    onNavigateBack = { selectedTab = 0 },
                    onNavigateToDiary = { selectedTab = 1 },
                    onNavigateToSearch = { showSearch = true }
                )
                3 -> AnalysisScreen(
                    onNavigateToChat = { showChat = true },
                    onNavigateToHealthProfile = { selectedTab = 4 }
                )
                4 -> ProfileScreen(onBack = onBack)
            }
        }

        if (!hideBottomBar) {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun markNotificationAsRead(id: Long, userId: Long) {
    Thread {
        runCatching {
            com.example.myapplication.api.RetrofitClient
                .getNotificationApiService().markAsRead(id, userId).execute()
        }
    }.start()
}

@Composable
private fun BottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    label = "首页",
                    isSelected = selectedTab == 0,
                    onClick = { onTabSelected(0) }
                )
                NavItem(
                    icon = if (selectedTab == 1) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                    label = "日记",
                    isSelected = selectedTab == 1,
                    onClick = { onTabSelected(1) }
                )

                Spacer(modifier = Modifier.width(56.dp))

                NavItem(
                    icon = if (selectedTab == 3) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                    label = "分析",
                    isSelected = selectedTab == 3,
                    onClick = { onTabSelected(3) }
                )
                NavItem(
                    icon = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
                    label = "我的",
                    isSelected = selectedTab == 4,
                    onClick = { onTabSelected(4) }
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .clickable { onTabSelected(2) },
            shape = CircleShape,
            color = MintGreen
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "识别",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) MintGreen else Color(0xFFBDBDBD),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MintGreen else Color(0xFFBDBDBD)
        )
    }
}
