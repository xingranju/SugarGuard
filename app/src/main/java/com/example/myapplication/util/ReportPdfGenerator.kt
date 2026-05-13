package com.example.myapplication.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportPdfGenerator {

    private const val PAGE_WIDTH = 595   // A4 width in points (72dpi)
    private const val PAGE_HEIGHT = 842  // A4 height in points
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    private val colorGreen = Color.parseColor("#26A69A")
    private val colorDark = Color.parseColor("#1A1A2E")
    private val colorSecondary = Color.parseColor("#6B7280")
    private val colorMuted = Color.parseColor("#9CA3AF")
    private val colorBg = Color.parseColor("#F5F7FA")
    private val colorAccent = Color.parseColor("#059669")
    private val colorWarning = Color.parseColor("#D97706")

    data class ReportData(
        val title: String,
        val periodLabel: String,
        val dateRange: String,
        val score: Int,
        val avgSugar: Float,
        val avgCalories: Float,
        val sugarLimit: Float,
        val overDays: Int,
        val totalDays: Int,
        val recordDays: Int,
        val aiReport: String?
    )

    fun generateAndShare(context: Context, data: ReportData) {
        try {
            val file = generatePdf(context, data)
            sharePdf(context, file, data.title)
        } catch (e: Exception) {
            Toast.makeText(context, "PDF生成失败: ${e.message?.take(40)}", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateAndSave(context: Context, data: ReportData) {
        try {
            val file = generatePdf(context, data)
            Toast.makeText(context, "PDF已保存: ${file.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "PDF生成失败: ${e.message?.take(40)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generatePdf(context: Context, data: ReportData): File {
        val document = PdfDocument()
        val pages = mutableListOf<PdfDocument.Page>()

        var pageNum = 1
        var currentPage = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
        )
        var canvas = currentPage.canvas
        var yPos = MARGIN

        yPos = drawHeader(canvas, yPos, data)
        yPos = drawScoreSection(canvas, yPos, data)
        yPos = drawStatsSection(canvas, yPos, data)

        if (!data.aiReport.isNullOrBlank()) {
            val result = drawAiReport(document, canvas, currentPage, yPos, pageNum, data.aiReport)
            currentPage = result.first
            canvas = currentPage.canvas
            yPos = result.second
            pageNum = result.third
        }

        yPos += 20f
        drawFooter(canvas, data)

        document.finishPage(currentPage)

        val reportsDir = File(context.filesDir, "pdf_reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(reportsDir, "糖知_${data.periodLabel}报告_$ts.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return file
    }

    private fun drawHeader(canvas: Canvas, startY: Float, data: ReportData): Float {
        var y = startY

        val headerPaint = Paint().apply {
            color = colorGreen
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            MARGIN - 10, y - 10, PAGE_WIDTH - MARGIN + 10, y + 70,
            12f, 12f, headerPaint
        )

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(data.title, MARGIN + 10, y + 25, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 255, 255, 255)
            textSize = 11f
        }
        canvas.drawText(data.dateRange, MARGIN + 10, y + 48, subPaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 255, 255, 255)
            textSize = 10f
        }
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("生成时间: $now", MARGIN + 10, y + 62, datePaint)

        return y + 90
    }

    private fun drawScoreSection(canvas: Canvas, startY: Float, data: ReportData): Float {
        var y = startY

        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(4f, 0f, 2f, Color.argb(20, 0, 0, 0))
        }
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 100, 10f, 10f, bgPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSecondary
            textSize = 12f
        }
        canvas.drawText("本${data.periodLabel}控糖评分", MARGIN + 20, y + 25, labelPaint)

        val scoreColor = when {
            data.score >= 90 -> colorAccent
            data.score >= 70 -> colorGreen
            data.score >= 50 -> colorWarning
            else -> Color.parseColor("#DC2626")
        }

        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = scoreColor
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("${data.score}", MARGIN + 20, y + 72, scorePaint)

        val maxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorMuted
            textSize = 14f
        }
        val scoreWidth = scorePaint.measureText("${data.score}")
        canvas.drawText("/100", MARGIN + 20 + scoreWidth + 4, y + 72, maxPaint)

        val scoreLabel = when {
            data.score >= 90 -> "优秀"
            data.score >= 70 -> "良好"
            data.score >= 50 -> "一般"
            else -> "需改善"
        }
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = scoreColor
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        val tagBg = Paint().apply {
            color = scoreColor
            alpha = 25
            style = Paint.Style.FILL
        }
        val tagX = PAGE_WIDTH - MARGIN - 80f
        canvas.drawRoundRect(tagX, y + 35, tagX + 60, y + 55, 6f, 6f, tagBg)
        canvas.drawText(scoreLabel, tagX + 12, y + 50, tagPaint)

        return y + 115
    }

    private fun drawStatsSection(canvas: Canvas, startY: Float, data: ReportData): Float {
        var y = startY

        val stats = listOf(
            Triple("日均糖分", "%.1fg".format(data.avgSugar), "目标 %.0fg".format(data.sugarLimit)),
            Triple("日均热量", "%.0fkcal".format(data.avgCalories), "${data.recordDays}天数据"),
            Triple("超标天数", "${data.overDays}天", "共${data.totalDays}天"),
            Triple("记录天数", "${data.recordDays}天", "共${data.totalDays}天")
        )

        val cardW = (CONTENT_WIDTH - 15) / 2
        for (i in stats.indices step 2) {
            val row = stats.subList(i, minOf(i + 2, stats.size))
            for ((j, stat) in row.withIndex()) {
                val x = MARGIN + j * (cardW + 15)
                drawStatCard(canvas, x, y, cardW, stat.first, stat.second, stat.third)
            }
            y += 65
        }

        return y + 5
    }

    private fun drawStatCard(
        canvas: Canvas, x: Float, y: Float, w: Float,
        label: String, value: String, detail: String
    ) {
        val bg = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(x, y, x + w, y + 55, 8f, 8f, bg)

        val border = Paint().apply {
            color = Color.parseColor("#F0F0F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(x, y, x + w, y + 55, 8f, 8f, border)

        val lp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorMuted; textSize = 10f }
        canvas.drawText(label, x + 12, y + 16, lp)

        val vp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorGreen; textSize = 18f; typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(value, x + 12, y + 36, vp)

        val dp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorMuted; textSize = 9f }
        canvas.drawText(detail, x + 12, y + 49, dp)
    }

    private data class DrawResult(val page: PdfDocument.Page, val y: Float, val pageNum: Int)

    private fun drawAiReport(
        document: PdfDocument,
        currentCanvas: Canvas,
        currentPage: PdfDocument.Page,
        startY: Float,
        startPageNum: Int,
        aiReport: String
    ): Triple<PdfDocument.Page, Float, Int> {
        var canvas = currentCanvas
        var page = currentPage
        var y = startY
        var pageNum = startPageNum

        fun ensureSpace(needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN - 30) {
                document.finishPage(page)
                pageNum++
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                )
                canvas = page.canvas
                y = MARGIN
            }
        }

        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorGreen
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        ensureSpace(30f)
        canvas.drawText("AI 营养教练分析", MARGIN, y + 15, sectionTitlePaint)
        y += 30

        val divPaint = Paint().apply { color = Color.parseColor("#F0F0F0"); strokeWidth = 1f }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, divPaint)
        y += 12

        val lines = aiReport.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                y += 8
                continue
            }
            when {
                trimmed.startsWith("【") && trimmed.endsWith("】") -> {
                    ensureSpace(28f)
                    y += 8
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorGreen; textSize = 13f; typeface = Typeface.DEFAULT_BOLD
                    }
                    canvas.drawText(trimmed, MARGIN, y + 14, p)
                    y += 22
                }
                trimmed.matches(Regex("^[一二三四五六七八九十]+[、.．].*")) -> {
                    ensureSpace(24f)
                    y += 6
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorDark; textSize = 12f; typeface = Typeface.DEFAULT_BOLD
                    }
                    canvas.drawText(trimmed, MARGIN, y + 14, p)
                    y += 22
                }
                trimmed.matches(Regex("^\\d+[.、．)].*")) -> {
                    val wrappedLines = wrapText(trimmed, CONTENT_WIDTH - 30, 11f)
                    ensureSpace(wrappedLines.size * 16f + 4f)
                    val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE; textSize = 8f; typeface = Typeface.DEFAULT_BOLD
                    }
                    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorGreen; style = Paint.Style.FILL
                    }
                    val num = trimmed.takeWhile { it.isDigit() }
                    canvas.drawCircle(MARGIN + 8, y + 10, 8f, circlePaint)
                    canvas.drawText(num, MARGIN + 5, y + 13, numPaint)

                    val content = trimmed.dropWhile { it.isDigit() }.trimStart('.', '、', '．', ')', ' ')
                    val contentLines = wrapText(content, CONTENT_WIDTH - 30, 11f)
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorSecondary; textSize = 11f
                    }
                    for (cl in contentLines) {
                        canvas.drawText(cl, MARGIN + 22, y + 14, textPaint)
                        y += 16
                    }
                    y += 4
                }
                trimmed.startsWith("- ") || trimmed.startsWith("· ") -> {
                    val content = trimmed.drop(2)
                    val wrappedLines = wrapText(content, CONTENT_WIDTH - 20, 11f)
                    ensureSpace(wrappedLines.size * 16f)
                    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorGreen; style = Paint.Style.FILL
                    }
                    canvas.drawCircle(MARGIN + 5, y + 9, 3f, dotPaint)
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorSecondary; textSize = 11f
                    }
                    for (cl in wrappedLines) {
                        canvas.drawText(cl, MARGIN + 16, y + 14, textPaint)
                        y += 16
                    }
                }
                else -> {
                    val wrappedLines = wrapText(trimmed, CONTENT_WIDTH, 11f)
                    ensureSpace(wrappedLines.size * 16f)
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorSecondary; textSize = 11f
                    }
                    for (cl in wrappedLines) {
                        canvas.drawText(cl, MARGIN, y + 14, textPaint)
                        y += 16
                    }
                }
            }
        }

        return Triple(page, y, pageNum)
    }

    private fun drawFooter(canvas: Canvas, data: ReportData) {
        val footerY = PAGE_HEIGHT - MARGIN.toFloat()
        val paint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, footerY - 15, PAGE_WIDTH - MARGIN, footerY - 15, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorMuted
            textSize = 9f
        }
        canvas.drawText("糖知 SugarGuard · AI 营养教练", MARGIN, footerY, textPaint)

        val rightText = "此报告由AI智能分析生成"
        val rightWidth = textPaint.measureText(rightText)
        canvas.drawText(rightText, PAGE_WIDTH - MARGIN - rightWidth, footerY, textPaint)
    }

    private fun wrapText(text: String, maxWidth: Float, textSize: Float): List<String> {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.textSize = textSize }
        val words = mutableListOf<String>()
        val result = mutableListOf<String>()

        for (ch in text) {
            words.add(ch.toString())
        }

        var currentLine = StringBuilder()
        for (word in words) {
            val test = currentLine.toString() + word
            if (paint.measureText(test) > maxWidth && currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) result.add(currentLine.toString())
        if (result.isEmpty()) result.add(text)
        return result
    }

    private fun sharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享PDF报告"))
    }
}
