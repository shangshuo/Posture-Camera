package com.posturecamera.app.util

import kotlin.math.abs

/**
 * 数学计算工具类
 */
object MathUtils {

    /**
     * 角度归一化函数
     * 将角度限制在 -180 到 180 度之间
     *
     * @param angle 输入角度（度）
     * @return 归一化后的角度（-180 ~ 180）
     */
    fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360
        if (normalized > 180) normalized -= 360
        if (normalized < -180) normalized += 360
        return normalized
    }

    /**
     * 计算两个角度之间的最短距离（处理 180/-180 边界）
     *
     * @param current 当前角度
     * @param baseline 基准角度
     * @return 最短角度偏差
     */
    fun getShortestAngleDiff(current: Float, baseline: Float): Float {
        val diff = current - baseline
        return when {
            diff > 180f -> diff - 360f
            diff < -180f -> diff + 360f
            else -> diff
        }
    }
}
