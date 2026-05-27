package com.posturecamera.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import com.posturecamera.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * 设备姿态管理器
 *
 * 使用 Android 系统级传感器融合（TYPE_ROTATION_VECTOR）获取设备姿态，
 * 无零点漂移，无需积分计算，直接获取绝对角度。
 *
 * 传感器融合原理：
 * - TYPE_ROTATION_VECTOR 内部融合了加速度计 + 陀螺仪 + 磁力计
 * - Android 系统自动处理传感器融合，输出四元数表示的旋转
 * - 通过 SensorManager.getRotationMatrixFromVector() 转换为旋转矩阵
 * - 通过 SensorManager.getOrientation() 从旋转矩阵提取欧拉角
 *
 * 坐标系说明：
 * - Android 设备坐标系：X轴朝右，Y轴朝上，Z轴朝屏幕外
 * - getOrientation() 返回 [azimuth, pitch, roll]（弧度）
 *   - azimuth：偏航角（绕Z轴旋转，手机水平时指北为0）
 *   - pitch：俯仰角（绕X轴旋转，手机顶部抬起为正，范围 -PI ~ PI）
 *   - roll：横滚角（绕Y轴旋转，手机左侧抬起为正，范围 -PI ~ PI）
 *
 * @param context 应用上下文
 */
class OrientationManager(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 优先使用 TYPE_ROTATION_VECTOR，回退到 TYPE_GAME_ROTATION_VECTOR
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

    // 当前使用的传感器类型（用于日志和调试）
    private val sensorType = rotationVectorSensor?.type ?: Sensor.TYPE_ROTATION_VECTOR

    // 旋转矩阵（从旋转向量转换而来）
    private val rotationMatrix = FloatArray(9)

    // 姿态角数组（从旋转矩阵提取）
    // [0] = azimuth（偏航），[1] = pitch（俯仰），[2] = roll（横滚）
    private val orientationAngles = FloatArray(3)

    // 姿态角度数据流
    private val _rotationAngles = MutableStateFlow(RotationAngles(0f, 0f, 0f))
    val rotationAngles: StateFlow<RotationAngles> = _rotationAngles.asStateFlow()

    // Roll 轴平滑处理变量
    // 原因：人手腕生理颤抖导致 Roll 变化率远高于 Pitch（Logcat 显示 Roll 变化率为 ±1-2°/sample，Pitch 仅为 ~0.02°/sample）
    // 需要针对性平滑 Roll 轴，提高用户体验
    private var smoothedRoll = 0f

    // 传感器监听器
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
                    it.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
                ) {
                    processRotationVector(it)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 不处理精度变化
        }
    }

    /**
     * 启动姿态传感器监听
     */
    fun startListening() {
        if (rotationVectorSensor == null) return

        sensorManager.registerListener(
            sensorListener,
            rotationVectorSensor,
            Constants.SENSOR_SAMPLING_PERIOD_US
        )
    }

    /**
     * 停止姿态传感器监听
     */
    fun stopListening() {
        sensorManager.unregisterListener(sensorListener)
    }

    /**
     * 检查姿态传感器是否可用
     *
     * @return true 如果设备支持旋转向量传感器
     */
    fun isOrientationSensorAvailable(): Boolean {
        return rotationVectorSensor != null
    }

    /**
     * 处理旋转向量传感器数据
     *
     * 核心处理流程：
     * 1. 从旋转向量获取旋转矩阵
     * 2. 重定向坐标系：将“手机垂直举起”定义为基准平面（Pitch=0），消除竖直状态下的奇点抖动
     * 3. 根据屏幕旋转方向进一步补偿
     * 4. 提取欧拉角并输出
     *
     * @param event 旋转向量传感器事件
     */
    private fun processRotationVector(event: SensorEvent) {
        // 步骤1：获取原始旋转矩阵
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        // 步骤2：坐标系重映射 - 核心修复
        // 默认 Android 坐标系中，手机水平平放时 Pitch=0，竖直时 Pitch=-90°（处于奇点）
        // 我们通过重映射，将 Y 轴（手机长边）映射到世界的 Z 轴（天顶），将 Z 轴映射到世界的 -Y 轴
        // 这样，当手机竖直举起时，在重映射后的坐标系中它是“水平”的，Pitch=0，Roll=0
        val baseRemappedMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            baseRemappedMatrix
        )

        // 步骤3：根据屏幕显示旋转方向补偿
        val displayRotation = getDisplayRotation()
        val finalMatrix = if (displayRotation == Surface.ROTATION_0) {
            baseRemappedMatrix
        } else {
            val compensatedMatrix = FloatArray(9)
            when (displayRotation) {
                Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                    baseRemappedMatrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    compensatedMatrix
                )
                Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                    baseRemappedMatrix,
                    SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_MINUS_Y,
                    compensatedMatrix
                )
                Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                    baseRemappedMatrix,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_X,
                    compensatedMatrix
                )
                else -> baseRemappedMatrix.copyInto(compensatedMatrix)
            }
            compensatedMatrix
        }

        // 步骤4：提取欧拉角
        SensorManager.getOrientation(finalMatrix, orientationAngles)

        // 转换为度数
        // 注意：经过重映射后：
        // pitch：手机前后倾斜（前倾为正，后仰为负）
        // roll：手机左右倾斜（左倾为正，右倾为负）
        val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
        val yaw = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

        // 输出数据
        _rotationAngles.value = RotationAngles(
            pitch = pitch,
            roll = roll,
            yaw = yaw
        )

        // 简化日志
        // if (System.currentTimeMillis() % 1000 < 20) {
        //     android.util.Log.d("OrientationManager", "Remapped - Pitch: $pitch, Roll: $roll")
        // }
    }

    /**
     * 获取屏幕旋转方向
     *
     * 通过 WindowManager 获取当前屏幕的旋转方向。
     * 优先使用 API 30+ 的 getDisplay() 方法，
     * 回退到已弃用的 defaultDisplay（API 30 以下）。
     *
     * @return 屏幕旋转方向（Surface.ROTATION_0/90/180/270）
     */
    @Suppress("DEPRECATION")
    private fun getDisplayRotation(): Int {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.display
            } else {
                windowManager.defaultDisplay
            }
            display?.rotation ?: Surface.ROTATION_0
        } catch (e: Exception) {
            Surface.ROTATION_0
        }
    }
}

/**
 * 旋转角度数据类
 *
 * @property pitch 俯仰角（度）：手机顶部抬起为正，顶部放下为负
 *                 水平放置时 pitch = 0，竖直时 pitch = -90
 * @property roll 横滚角（度）：手机左侧抬起为正，右侧抬起为负
 *                水平放置时 roll = 0
 * @property yaw 偏航角（度）：手机水平时指北为0，顺时针为正
 *               注意：体态相机应用不使用偏航角，此值仅记录
 */
data class RotationAngles(
    val pitch: Float,
    val roll: Float,
    val yaw: Float
)
