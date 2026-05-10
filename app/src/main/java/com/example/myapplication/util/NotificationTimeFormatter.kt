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

    // 无时区格式；后端 application.yml 固定以 GMT+8 序列化，因此解析时也必须按 GMT+8，
    // 否则设备默认时区（如模拟器 GMT）会导致最大 8 小时的时间错位。[BUG-021]
    private val LOCAL_PATTERNS = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
        "yyyy-MM-dd HH:mm"
    )

    private val SERVER_TIME_ZONE: TimeZone = TimeZone.getTimeZone("GMT+8")

    // 未来时间的容错阈值：12 小时以内的未来时间一律视为时钟/时区漂移，显示为「刚刚」。[BUG-021]
    private const val FUTURE_CLOCK_SKEW_TOLERANCE_MS = 12L * 60L * 60L * 1000L

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
                    timeZone = SERVER_TIME_ZONE
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
        // [BUG-021] 未来时间处理：当通知时间相对设备时间在未来 12 小时以内，
        // 基本可以判定为设备时区 / 时钟漂移导致的偏差，一律显示为「刚刚」而不是具体绝对时间，
        // 避免用户看到"刚点击发送、却被标注成几小时前/后"的体验。
        if (diff < 0) {
            return if (-diff < FUTURE_CLOCK_SKEW_TOLERANCE_MS) "刚刚" else formatAbsolute(Date(millis))
        }
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

    // [BUG-021] 绝对时间一律按后端声明的 GMT+8 展示，保证客户端显示值与服务端业务时间一致，
    // 不受设备默认时区影响（中国用户永远看到北京时间字面值）。
    private fun formatAbsolute(date: Date): String {
        return SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())
            .apply { timeZone = SERVER_TIME_ZONE }
            .format(date)
    }
}
