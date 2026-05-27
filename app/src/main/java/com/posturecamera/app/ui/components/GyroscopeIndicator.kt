package com.posturecamera.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.posturecamera.app.ui.theme.AlignedGreen
import com.posturecamera.app.ui.theme.BoundaryRed
import com.posturecamera.app.ui.theme.NormalOrange
import com.posturecamera.app.util.Constants
import kotlin.math.abs

/**
 * 圆点状态枚举
 */
enum class CircleState {
    ALIGNED,           // 极小偏差: 绿色,放大直径
    NORMAL,            // 正常范围内: 橙色,正常直径
    BOUNDARY_WARNING   // 超过边界阈值: 红色,放大直径
}

/**
 * 自适应圆点直径数据类
 *
 * @property normalRadius 正常状态直径
 * @property alignedRadius 对齐/边界警告状态直径
 */
data class AdaptiveCircleRadius(
    val normalRadius: Dp,
    val alignedRadius: Dp
)

/**
 * 计算自适应圆点直径
 *
 * 基于屏幕宽度百分比计算,保证跨设备一致的视觉体验
 * - 正常直径: 屏幕宽度的4%
 * - 对齐/警告直径: 屏幕宽度的5.75%
 */
@Composable
fun rememberAdaptiveCircleRadius(): AdaptiveCircleRadius {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    return remember(screenWidthDp) {
        AdaptiveCircleRadius(
            normalRadius = (screenWidthDp * 0.04f).dp,
            alignedRadius = (screenWidthDp * 0.0575f).dp
        )
    }
}

/**
 * 计算圆点状态
 *
 * @param deviation 偏离正确姿势的角度偏差（度）
 * @param boundaryThreshold 边界警告阈值（度）
 * @return 圆点状态
 */
fun calculateCircleState(deviation: Float, boundaryThreshold: Float): CircleState {
    return when {
        deviation < Constants.ALIGNMENT_THRESHOLD -> CircleState.ALIGNED
        deviation >= boundaryThreshold -> CircleState.BOUNDARY_WARNING
        else -> CircleState.NORMAL
    }
}

/**
 * 获取状态对应的颜色
 *
 * @param state 圆点状态
 * @return 对应的颜色
 */
fun getColorForState(state: CircleState): Color {
    return when (state) {
        CircleState.ALIGNED -> AlignedGreen.copy(alpha = 0.6f)
        CircleState.NORMAL -> NormalOrange.copy(alpha = 0.6f)
        CircleState.BOUNDARY_WARNING -> BoundaryRed.copy(alpha = 0.6f)
    }
}

/**
 * 获取状态对应的直径
 *
 * @param state 圆点状态
 * @param adaptiveRadius 自适应直径配置
 * @return 对应的直径
 */
fun getRadiusForState(state: CircleState, adaptiveRadius: AdaptiveCircleRadius): Dp {
    return when (state) {
        CircleState.ALIGNED -> adaptiveRadius.alignedRadius
        CircleState.BOUNDARY_WARNING -> adaptiveRadius.alignedRadius
        CircleState.NORMAL -> adaptiveRadius.normalRadius
    }
}

/**
 * 陀螺仪双圆点指示器
 *
 * 基于重构后的坐标系：
 * - 输入的 pitch 和 roll 已经是经过坐标重映射后的偏差值（直立状态为 0,0）
 * - 无需再进行任何基准补偿（如 +90 或 -180）
 *
 * @param pitch 俯仰角偏差(度), 手机前倾为正，后仰为负
 * @param roll 横滚角偏差(度), 手机左倾为正，右倾为负
 * @param modifier 修饰符
 */
@Composable
fun GyroscopeIndicator(
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    val adaptiveRadius = rememberAdaptiveCircleRadius()

    // 安全处理异常角度值
    val safePitch = if (pitch.isNaN() || pitch.isInfinite()) 0f else pitch
    val safeRoll = if (roll.isNaN() || roll.isInfinite()) 0f else roll

    // 计算偏差绝对值用于状态判定
    val pitchAbsDeviation = abs(safePitch)
    val rollAbsDeviation = abs(safeRoll)

    // Y轴圆点状态
    val yState = calculateCircleState(pitchAbsDeviation, Constants.PITCH_BOUNDARY_THRESHOLD)

    // Y轴圆点动画
    val yRadius by animateDpAsState(
        targetValue = getRadiusForState(yState, adaptiveRadius),
        animationSpec = tween(
            durationMillis = Constants.ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "yRadius"
    )

    val yColor by animateColorAsState(
        targetValue = getColorForState(yState),
        animationSpec = tween(Constants.ANIMATION_DURATION_MS),
        label = "yColor"
    )

    // X轴圆点状态
    val xState = calculateCircleState(rollAbsDeviation, Constants.ROLL_BOUNDARY_THRESHOLD)

    // X轴圆点动画
    val xRadius by animateDpAsState(
        targetValue = getRadiusForState(xState, adaptiveRadius),
        animationSpec = tween(
            durationMillis = Constants.ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "xRadius"
    )

    val xColor by animateColorAsState(
        targetValue = getColorForState(xState),
        animationSpec = tween(Constants.ANIMATION_DURATION_MS),
        label = "xColor"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = this.center
        val centerX = center.x
        val centerY = center.y

        // 计算最大像素偏移距离
        val maxOffsetX = size.width / 2 - xRadius.toPx()
        val maxOffsetY = size.height / 2 - yRadius.toPx()

        // 1. 计算 Y 轴圆点位置
        // 逻辑：前倾(pitch>0) -> 圆点向下(y增大)；后仰(pitch<0) -> 圆点向上(y减小)
        val y = when {
            abs(safePitch) <= Constants.PITCH_DISPLAY_RANGE -> {
                val ratio = safePitch / Constants.PITCH_DISPLAY_RANGE
                centerY + maxOffsetY * ratio // 前倾为正，向下移动
            }
            safePitch > Constants.PITCH_DISPLAY_RANGE -> centerY + maxOffsetY // 底部边缘
            else -> centerY - maxOffsetY // 顶部边缘
        }

        // 2. 计算 X 轴圆点位置
        // 逻辑：左倾(roll>0) -> 圆点向左(x减小)；右倾(roll<0) -> 圆点向右(x增大)
        val x = when {
            abs(safeRoll) <= Constants.ROLL_DISPLAY_RANGE -> {
                val ratio = safeRoll / Constants.ROLL_DISPLAY_RANGE
                centerX - maxOffsetX * ratio // 左倾为正，向左移动
            }
            safeRoll > Constants.ROLL_DISPLAY_RANGE -> centerX - maxOffsetX // 左侧边缘
            else -> centerX + maxOffsetX // 右侧边缘
        }

        // 绘制参考线
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        // 绘制Y轴圆点（在垂直线上移动）
        drawCircle(
            color = yColor,
            radius = yRadius.toPx(),
            center = Offset(centerX, y)
        )

        // 绘制X轴圆点（在水平线上移动）
        drawCircle(
            color = xColor,
            radius = xRadius.toPx(),
            center = Offset(x, centerY)
        )
    }
}
