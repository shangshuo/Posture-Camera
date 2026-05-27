package com.posturecamera.app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posturecamera.app.data.repository.PhotoRepository
import com.posturecamera.app.processor.WatermarkProcessor
import com.posturecamera.app.sensor.OrientationManager
import com.posturecamera.app.sensor.RotationAngles
import com.posturecamera.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

// 将 ViewModelFactory 移到独立文件以提高可读性

/**
 * 相机ViewModel
 */
class CameraViewModel(
    private val repository: PhotoRepository,
    private val context: Context
) : ViewModel() {

    private lateinit var orientationManager: OrientationManager
    private val watermarkProcessor = WatermarkProcessor()

    private val _rotationAngles = MutableStateFlow(RotationAngles(0f, 0f, 0f))
    val rotationAngles: StateFlow<RotationAngles> = _rotationAngles.asStateFlow()

    private val _pitchDeviation = MutableStateFlow(0f)
    val pitchDeviation: StateFlow<Float> = _pitchDeviation.asStateFlow()

    private val _rollDeviation = MutableStateFlow(0f)
    val rollDeviation: StateFlow<Float> = _rollDeviation.asStateFlow()

    private val _isAligned = MutableStateFlow(false)
    val isAligned: StateFlow<Boolean> = _isAligned.asStateFlow()

    private val _captureState = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    /**
     * 初始化姿态传感器
     */
    fun initializeOrientationSensor() {
        orientationManager = OrientationManager(context)

        if (!orientationManager.isOrientationSensorAvailable()) {
            return
        }

        viewModelScope.launch {
            orientationManager.rotationAngles.collect { angles ->
                _rotationAngles.value = angles
                
                // 经过重映射后，目标姿态就是 (0,0)
                _pitchDeviation.value = angles.pitch
                _rollDeviation.value = angles.roll
                
                _isAligned.value = isAligned(angles.pitch, angles.roll)
            }
        }

        orientationManager.startListening()
    }


    /**
     * 检测设备是否处于正确的拍照姿势
     *
     * 正确拍照姿势（经过坐标系重映射后）：
     * - 手机竖直垂直于地面：pitch 接近 0
     * - 手机左右水平：roll 接近 0
     *
     * @param pitch 俯仰角偏差（度）
     * @param roll 横滚角偏差（度）
     * @return true 如果设备处于正确拍照姿势
     */
    private fun isAligned(pitch: Float, roll: Float): Boolean {
        return kotlin.math.abs(pitch) < Constants.ALIGNMENT_THRESHOLD &&
                kotlin.math.abs(roll) < Constants.ALIGNMENT_THRESHOLD
    }

    /**
     * 处理拍照
     */
    fun onImageCaptured(imageProxy: ImageProxy) {
        viewModelScope.launch {
            _captureState.value = CaptureState.Processing

            try {
                val bitmap = withContext(Dispatchers.IO) {
                    imageProxyToBitmap(imageProxy)
                }

                val angles = _rotationAngles.value
                val timestamp = System.currentTimeMillis()

                val watermarkedBitmap = withContext(Dispatchers.IO) {
                    watermarkProcessor.addWatermark(
                        bitmap = bitmap,
                        timestamp = timestamp,
                        pitch = angles.pitch,
                        roll = angles.roll
                    )
                }

                val result = repository.savePhoto(
                    bitmap = watermarkedBitmap,
                    timestamp = timestamp,
                    pitch = angles.pitch,
                    roll = angles.roll,
                    yaw = angles.yaw
                )

                result.fold(
                    onSuccess = {
                        _captureState.value = CaptureState.Success("照片已保存到相册")
                    },
                    onFailure = { exception ->
                        _captureState.value = CaptureState.Error(exception.message ?: "保存失败")
                    }
                )
            } catch (e: Exception) {
                _captureState.value = CaptureState.Error(e.message ?: "处理失败")
            } finally {
                imageProxy.close()
            }
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // 处理旋转
        val rotationDegrees = image.imageInfo.rotationDegrees
        if (rotationDegrees == 0) return bitmap

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }

        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

    fun resetCaptureState() {
        _captureState.value = CaptureState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        if (::orientationManager.isInitialized) {
            orientationManager.stopListening()
        }
    }
}

/**
 * 拍照状态
 */
sealed class CaptureState {
    object Idle : CaptureState()
    object Processing : CaptureState()
    data class Success(val message: String) : CaptureState()
    data class Error(val message: String) : CaptureState()
}
