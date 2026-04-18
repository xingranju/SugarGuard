package com.example.myapplication.util

import com.example.myapplication.api.RetrofitClient
import java.io.File

/**
 * 将日记接口返回的 imagePath 转为 Coil 可加载的数据源（绝对 URL、本地文件或 null）。
 */
fun resolveMealImageData(path: String?): Any? {
    if (path.isNullOrBlank()) return null
    val trimmed = path.trim()
    if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
        return trimmed
    }
    val f = File(trimmed)
    if (f.exists() && f.isFile) return f
    val base = RetrofitClient.getBaseUrl().trimEnd('/')
    return if (trimmed.startsWith("/")) base + trimmed else "$base/$trimmed"
}
