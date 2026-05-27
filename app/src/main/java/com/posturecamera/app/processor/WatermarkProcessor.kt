package com.posturecamera.app.processor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.posturecamera.app.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 水印处理器
 */
class WatermarkProcessor {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    /**
     * 添加水印到照片
     *
     * @param bitmap 原始图片
     * @param timestamp 时间戳
     * @param pitch 俯仰角
     * @param roll 横滚角
     * @return 带水印的图片
     */
    fun addWatermark(
        bitmap: Bitmap,
        timestamp: Long,
        pitch: Float,
        roll: Float
    ): Bitmap {
        // 创建可修改的 Bitmap 副本
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // 格式化日期时间
        val date = Date(timestamp)
        val dateLine = dateFormat.format(date)
        val timeLine = timeFormat.format(date)
        val angleLine = "姿态: 俯仰 %.1f° 横滚 %.1f°".format(pitch, roll)

        // 计算动态缩放比例（基于宽度 1080px 作为基准）
        val scale = bitmap.width / 1080f
        val dynamicTextSize = Constants.WATERMARK_TEXT_SIZE * scale
        val dynamicLineHeight = Constants.WATERMARK_LINE_HEIGHT * scale
        val dynamicPadding = Constants.WATERMARK_PADDING * scale

        // 文字画笔
        val textPaint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            textSize = dynamicTextSize
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }

        // 计算文字尺寸
        val maxTextWidth = floatArrayOf(
            textPaint.measureText(dateLine),
            textPaint.measureText(timeLine),
            textPaint.measureText(angleLine)
        ).maxOrNull() ?: 0f

        // 计算背景框位置（右下角）
        val backgroundRect = RectF(
            bitmap.width - maxTextWidth - dynamicPadding * 3,
            bitmap.height - dynamicLineHeight * 3 - dynamicPadding * 3,
            bitmap.width - dynamicPadding,
            bitmap.height - dynamicPadding
        )

        // 绘制半透明黑色背景框
        val backgroundPaint = Paint().apply {
            color = Color.BLACK
            alpha = 120
            style = Paint.Style.FILL
        }
        canvas.drawRect(backgroundRect, backgroundPaint)

        // 绘制文字（3行）
        var textY = backgroundRect.top + dynamicLineHeight
        canvas.drawText(dateLine, backgroundRect.left + dynamicPadding, textY, textPaint)

        textY += dynamicLineHeight
        canvas.drawText(timeLine, backgroundRect.left + dynamicPadding, textY, textPaint)

        textY += dynamicLineHeight
        canvas.drawText(angleLine, backgroundRect.left + dynamicPadding, textY, textPaint)

        return mutableBitmap
    }
}
