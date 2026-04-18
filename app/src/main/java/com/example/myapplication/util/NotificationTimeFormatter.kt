package com.example.myapplication.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 通知时间格式化工具：
 * - 支持后端返回的多种时间格式（ISO 8601 带/不带时区，"yyyy-MM-dd HH:mm:ss" 等）
 * - 输出本地化的「刚刚 / X分钟前 / X小时前 / X天前 / M月d日 HH:mm」
 * - 解析失败时降级返回原字符串，避免误判为「刚刚」
 */
object NotificationTimeFormatter {

    private val ISO_OFFSET_PATTERNS = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )

    // 无时区格式（按本地时区解析）
    private val LOCAL_PATTERNS = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
        "yyyy-MM-dd HH:mm"
    )

    /**
     * 将后端时间字符串解析为毫秒时间戳。解析失败返回 null。
     */
    fun parseToMillis(dateTimeStr: String?): Long? {
        if (dateTimeStr.isNullOrBlank()) return null
        val raw = dateTimeStr.trim()

        for (p in ISO_OFFSET_PATTERNS) {
            runCatching {
                val sdf = SimpleDateFormat(p, Locale.US).apply { isLenient = false }
                sdf.parse(raw)?.let { return it.time }
            }
        }
        for (p in LOCAL_PATTERNS) {
            runCatching {
                val sdf = SimpleDateFormat(p, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getDefault()
                }
                sdf.parse(raw)?.let { return it.time }
            }
        }
        return null
    }

    fun formatRelative(dateTimeStr: String?): String {
        if (dateTimeStr.isNullOrBlank()) return ""
        val millis = parseToMillis(dateTimeStr) ?: return dateTimeStr
        val now = System.currentTimeMillis()
        var diff = now - millis
        // 允许 60 秒以内的未来偏差（时钟漂移），其他未来时间也用「刚刚」
        if (diff < -60_000L) {
            val absSec = (-diff) / 1000
            return when {
                absSec < 60 -> "刚刚"
                else -> formatAbsolute(Date(millis))
            }
        }
        if (diff < 0) diff = 0
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return when {
            seconds < 30 -> "刚刚"
            minutes < 1 -> "${seconds}秒前"
            minutes < 60 -> "${minutes}分钟前"
            hours < 24 -> "${hours}小时前"
            days < 7 -> "${days}天前"
            else -> formatAbsolute(Date(millis))
        }
    }

    private fun formatAbsolute(date: Date): String {
        return SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(date)
    }
}
