package com.example.myapplication.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.viewmodel.AIServiceViewModel

/**
 * 食物识别Activity (已重定向到AI服务)
 * shiWu_shibie_Activity
 */
class FoodRecognitionActivity : ComponentActivity() {
    private lateinit var viewModel: AIServiceViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 使用AIServiceViewModel
        viewModel = ViewModelProvider(this)[AIServiceViewModel::class.java]
        
        // 获取用户ID
        val userId = intent.getIntExtra("USER_ID", 1)
        
        setContent {
            SugarGuardTheme {
                Surface(
                    modifier = Modifier
                ) {
                    // 使用新的AIServiceScreen
                    AIServiceScreen(
                        viewModel = viewModel,
                        userId = userId,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

