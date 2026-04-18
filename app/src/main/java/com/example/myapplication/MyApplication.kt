package com.example.myapplication

import android.app.Application
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.db.DatabaseProvider
import com.example.myapplication.notification.NotificationPollWorker
import com.example.myapplication.notification.SugarGuardNotifications
import java.util.concurrent.TimeUnit

class MyApplication : Application() {
    
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Room本地数据库
        DatabaseProvider.init(this)
        Log.d("MyApplication", "Room数据库已初始化")
        
        // 初始化RetrofitClient（保留用于AI识别等网络功能）
        RetrofitClient.init(this)
        Log.d("MyApplication", "RetrofitClient已初始化")

        SugarGuardNotifications.ensureChannel(this)
        val notifWork = PeriodicWorkRequestBuilder<NotificationPollWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sugarguard_notif_poll",
            ExistingPeriodicWorkPolicy.KEEP,
            notifWork
        )
        
        // 保存默认异常处理器
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("MyApplication", "=== 应用崩溃 ===", throwable)
                Log.e("MyApplication", "线程: ${thread.name}")
                Log.e("MyApplication", "异常: ${throwable.javaClass.name}")
                Log.e("MyApplication", "消息: ${throwable.message}")
                throwable.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                defaultExceptionHandler?.uncaughtException(thread, throwable)
            }
        }
        
        Log.d("MyApplication", "应用已启动")
    }
}

